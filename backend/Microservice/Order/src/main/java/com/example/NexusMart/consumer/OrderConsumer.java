// File: backend/Microservice/Order/src/main/java/com/example/NexusMart/consumer/OrderConsumer.java
package com.example.NexusMart.consumer;

import com.example.NexusMart.config.OrderStatusWebSocketHandler;
import com.example.NexusMart.exception.ResourceNotFoundException;
import com.example.NexusMart.model.Order;
import com.example.NexusMart.pojoClass.*;
import com.example.NexusMart.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class OrderConsumer {

    @Value("${cartservice.url}")
    private String cartServiceUrl;

    @Value("${itemservice.url}")
    private String itemServiceUrl;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusWebSocketHandler orderStatusWebSocketHandler;

    @KafkaListener(topics = "paymentResultTopic", groupId = "order-group")
    @Transactional
    public void handlePaymentResult(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            PaymentResult paymentResult = objectMapper.readValue(record.value(), PaymentResult.class);
            log.info("Received payment result for order ID: " + paymentResult.getOrderId());

            Order order = orderRepository.findById(paymentResult.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order", "id", paymentResult.getOrderId().toString()));

            if (paymentResult.isPaymentSuccessful()) {
                order.setStatus("COMPLETED");
                clearCart(paymentResult.getCartId(), paymentResult.getToken());
                log.info("Order status updated for order ID: " + paymentResult.getOrderId() + ", status: " + order.getStatus());
            }
            else {
                order.getOrderItems().forEach(orderItem -> {
                    Item item = getItemById(orderItem.getItemId(), paymentResult.getToken());
                    int newQuantity = item.getQuantity() + orderItem.getQuantity();
                    updateItemQuantity(orderItem.getItemId(), newQuantity);
                });
                order.setStatus("FAILED");
                log.info("Order failed for order ID: " + paymentResult.getOrderId() + " due to failed payment");
            }

            orderRepository.save(order);
            acknowledgment.acknowledge();
            log.info("Order consumer: order id = " + order.getId());
            orderStatusWebSocketHandler.notifyOrderStatusChange(order.getId(), order.getStatus());
        } catch (JsonProcessingException e) {
            log.error("Error deserializing payment result message", e);
        } catch (Exception e) {
            log.error("Exception in handlePaymentResult", e);
        }
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

    private void updateItemQuantity(Long itemId, int newQuantity) {
        String url = String.format("%s/%s/quantity", itemServiceUrl, itemId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Item item = new Item();
        item.setId(itemId);
        item.setQuantity(newQuantity);
        HttpEntity<Item> requestEntity = new HttpEntity<>(item, headers);
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to update item quantity for item: " + itemId);
        }
    }

    private void clearCart(Long cartId, String token) {
        String url = String.format("%s/clear/%s", cartServiceUrl, cartId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        restTemplate.exchange(url, HttpMethod.POST, requestEntity, Void.class);
        log.info("Cart cleared for cart ID: " + cartId);
    }
}