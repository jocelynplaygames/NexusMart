// File: backend/Microservice/Order/src/main/java/com/example/NexusMart/service/OrderService.java
package com.example.NexusMart.service;

import com.example.NexusMart.config.DataSourceType;
import com.example.NexusMart.config.OrderStatusWebSocketHandler;
import com.example.NexusMart.config.ReplicationRoutingDataSourceContext;
import com.example.NexusMart.dto.OrderDTO;
import com.example.NexusMart.exception.ResourceNotFoundException;
import com.example.NexusMart.mapper.OrderMapper;
import com.example.NexusMart.model.*;
import com.example.NexusMart.pojoClass.*;
import com.example.NexusMart.repository.OrderRepository;
import com.example.NexusMart.repository.ShippingMethodRepository;
import com.example.NexusMart.utils.KafkaHealthCheck;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {

    private final ShippingMethodRepository shippingMethodsRepository;
    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;
    private final OrderMapper orderMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaHealthCheck kafkaHealthCheck;

    @Value("${userservice.url}")
    private String userServiceUrl;

    @Value("${itemservice.url}")
    private String itemServiceUrl;

    @Value("${cartservice.url}")
    private String cartServiceUrl;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderStatusWebSocketHandler orderStatusWebSocketHandler;

    @Autowired
    public OrderService(ShippingMethodRepository shippingMethodsRepository, OrderRepository orderRepository,
                        RestTemplate restTemplate, OrderMapper orderMapper) {
        this.shippingMethodsRepository = shippingMethodsRepository;
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
        this.orderMapper = orderMapper;
    }

    public List<ShippingMethod> getShippingMethods() {
        return shippingMethodsRepository.findAll();
    }

    public Optional<OrderDTO> getOrder(Long orderId) {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.SLAVE);
        try {
            return orderRepository.findById(orderId).map(orderMapper::toDTO);
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    public List<OrderDTO> getOrdersByUser(String userId) {
        log.info("Getting user orders!");
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.SLAVE);
        try {
            return orderRepository.findByUserId(userId).stream().map(orderMapper::toDTO).collect(Collectors.toList());
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    public Long createOrder(Long cartId, Long shippingMethodId, Long cardId, String token) {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.MASTER);
        try {
            Cart cart = getCartById(cartId, token);
            ShippingMethod shippingMethod = shippingMethodsRepository.findById(shippingMethodId)
                    .orElseThrow(() -> new ResourceNotFoundException("ShippingMethod", "id", shippingMethodId.toString()));
            Card card = getCardById(cardId, token);

            log.info("Order items length: " + cart.getCartItemsOutput().size());

            double itemsTotal = cart.getCartItemsOutput().stream()
                    .mapToDouble(cartItem -> {
                        Item item = getItemById(cartItem.getItemId(), token);
                        if (cartItem.getQuantity() > item.getQuantity()) {
                            throw new IllegalArgumentException("Insufficient quantity for item: " + item.getId());
                        }
                        return cartItem.getQuantity() * item.getPrice();
                    })
                    .sum();
            double shippingCost = shippingMethod.getPrice();
            double tax = (itemsTotal + shippingCost) * 0.06;
            double totalAmount = itemsTotal + shippingCost + tax;

            Order order = new Order();
            order.setUserId(cart.getUserId());
            order.setTotalAmount(totalAmount);
            order.setStatus("PENDING");
            order.setShippingMethod(shippingMethod);
            order.setCardType(card.getType());
            order.setLastFourDigit(card.getCardNumber().substring(card.getCardNumber().length() - 4));

            List<OrderItem> orderItems = cart.getCartItemsOutput().stream()
                    .map(cartItem -> {
                        OrderItem orderItem = new OrderItem();
                        orderItem.setOrder(order);
                        orderItem.setItemId(cartItem.getItemId());
                        orderItem.setQuantity(cartItem.getQuantity());
                        Item item = getItemById(cartItem.getItemId(), token);
                        orderItem.setPrice(item.getPrice());
                        return orderItem;
                    })
                    .collect(Collectors.toList());
            order.setOrderItems(orderItems);

            Order savedOrder = orderRepository.save(order);
            orderStatusWebSocketHandler.notifyOrderStatusChange(savedOrder.getId(), "PENDING");
            log.info("Saved order id: " + savedOrder.getId());

            orderItems.forEach(orderItem -> {
                Item item = getItemById(orderItem.getItemId(), token);
                int newQuantity = item.getQuantity() - orderItem.getQuantity();
                updateItemQuantity(orderItem.getItemId(), newQuantity);
            });

            if (kafkaHealthCheck.areMultipleBrokersAvailable()) {
                try {
                    PaymentRequest paymentRequest = new PaymentRequest(savedOrder.getId(), totalAmount, cartId, token);
                    String paymentRequestJson = objectMapper.writeValueAsString(paymentRequest);
                    kafkaTemplate.send("payEvent", paymentRequestJson).get();
                    log.info("Payment request sent to Kafka for order ID: " + savedOrder.getId() + ", amount: " + totalAmount);
                } catch (JsonProcessingException | InterruptedException | ExecutionException e) {
                    savedOrder.setStatus("FAILED");
                    orderStatusWebSocketHandler.notifyOrderStatusChange(savedOrder.getId(), "FAILED");
                    orderRepository.save(savedOrder);
                    orderItems.forEach(orderItem -> {
                        Item item = getItemById(orderItem.getItemId(), token);
                        int newQuantity = item.getQuantity() + orderItem.getQuantity();
                        updateItemQuantity(orderItem.getItemId(), newQuantity);
                    });
                    throw new RuntimeException("Error sending payment request to Kafka", e);
                }
            } else {
                savedOrder.setStatus("FAILED");
                orderStatusWebSocketHandler.notifyOrderStatusChange(savedOrder.getId(), "FAILED");
                orderRepository.save(savedOrder);
                orderItems.forEach(orderItem -> {
                    Item item = getItemById(orderItem.getItemId(), token);
                    int newQuantity = item.getQuantity() + orderItem.getQuantity();
                    updateItemQuantity(orderItem.getItemId(), newQuantity);
                });
                throw new RuntimeException("Not enough brokers available to send the message");
            }

            return savedOrder.getId();
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    private void updateItemQuantity(Long itemId, int newQuantity) {
        String url = String.format("%s/%s/quantity", itemServiceUrl, itemId);
        HttpHeaders headers = new HttpHeaders();
        Item item = new Item();
        item.setId(itemId);
        item.setQuantity(newQuantity);
        HttpEntity<Item> requestEntity = new HttpEntity<>(item, headers);
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to update item quantity for item: " + itemId);
        }
    }


    private Cart getCartById(Long cartId, String token) {
        String url = String.format("%s/cartId/%s", cartServiceUrl, cartId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Cart> cartResponse = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Cart.class);
        Cart cart = cartResponse.getBody();
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "id", cartId.toString());
        }
        return cart;
    }

    private Card getCardById(Long cardId, String token) {
        String url = String.format("%s/card/%s", userServiceUrl, cardId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Card> cardResponse = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Card.class);
        Card card = cardResponse.getBody();
        if (card == null) {
            throw new ResourceNotFoundException("Card", "id", cardId.toString());
        }
        return card;
    }

    private Item getItemById(Long itemId, String token) {
        String url = String.format("%s/%s", itemServiceUrl, itemId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Item> itemResponse = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Item.class);
        Item item = itemResponse.getBody();
        if (item == null) {
            throw new ResourceNotFoundException("Item", "id", itemId.toString());
        }
        log.info("Order item id: " + item.getId());
        return item;
    }
}

