package com.example.NexusMart.mapper;

import com.example.NexusMart.dto.OrderDTO;
import com.example.NexusMart.dto.OrderItemDTO;
import com.example.NexusMart.exception.ResourceNotFoundException;
import com.example.NexusMart.model.*;
import com.example.NexusMart.pojoClass.Card;
import com.example.NexusMart.pojoClass.Item;
import com.example.NexusMart.pojoClass.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${itemservice.url}")
    private String itemServiceUrl;

    @Value("${userservice.url}")
    private String userServiceUrl;

    @Value("${cardservice.url}")
    private String cardServiceUrl;

    @Value("${orderservice.url}")
    private String orderServiceUrl;

    public OrderDTO toDTO(Order order) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(order.getId());
        orderDTO.setUserId(order.getUserId());
        User user = getUserById(order.getUserId());
        orderDTO.setUserName(user.getUsername());
        orderDTO.setTotalAmount(order.getTotalAmount());
        orderDTO.setStatus(order.getStatus());
        orderDTO.setCreatedAt(order.getCreatedAt());
        orderDTO.setUpdatedAt(order.getUpdatedAt());
        orderDTO.setCardType(order.getCardType());
        orderDTO.setLastFourDigit(order.getLastFourDigit());
        orderDTO.setShippingMethodId(order.getShippingMethod().getId());
        orderDTO.setShippingMethodName(order.getShippingMethod().getName());
        orderDTO.setShippingCost(order.getShippingMethod().getPrice()); 

        
        List<OrderItemDTO> orderItems = order.getOrderItems().stream()
                .map(this::toOrderItemDTO)
                .collect(Collectors.toList());
        orderDTO.setOrderItems(orderItems);

        return orderDTO;
    }

    private OrderItemDTO toOrderItemDTO(OrderItem orderItem) {
        OrderItemDTO orderItemDTO = new OrderItemDTO();
        orderItemDTO.setItemId(orderItem.getItemId());
        orderItemDTO.setQuantity(orderItem.getQuantity());
        orderItemDTO.setPrice(orderItem.getPrice());
        Item item = getItemById(orderItem.getItemId());
        orderItemDTO.setItemName(item.getTitle());
        return orderItemDTO;
    }

    public Order toEntity(OrderDTO orderDTO, Long cardId, Long shippingMethodId) {
        Order order = new Order();
        order.setId(orderDTO.getId());
        order.setUserId(orderDTO.getUserId());
        order.setTotalAmount(orderDTO.getTotalAmount());
        order.setStatus(orderDTO.getStatus());
        Card card = getCardById(cardId);
        order.setCardType(card.getType());
        order.setLastFourDigit(card.getCardNumber().substring(card.getCardNumber().length() - 4));
        ShippingMethod shippingMethod = getShippingMethodById(shippingMethodId);
        order.setShippingMethod(shippingMethod);

        
        List<OrderItem> orderItems = orderDTO.getOrderItems().stream()
                .map(orderItemDTO -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setItemId(orderItemDTO.getItemId());
                    orderItem.setQuantity(orderItemDTO.getQuantity());
                    orderItem.setPrice(orderItemDTO.getPrice());
                    return orderItem;
                })
                .collect(Collectors.toList());
        order.setOrderItems(orderItems);

        return order;
    }

    private User getUserById(String userId) {
        String url = String.format("%s/%s", userServiceUrl, userId);
        ResponseEntity<User> userResponse = restTemplate.getForEntity(url, User.class);
        User user = userResponse.getBody();
        if (user == null) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        return user;
    }

    private Item getItemById(Long itemId) {
        String url = String.format("%s/%s", itemServiceUrl, itemId);
        ResponseEntity<Item> itemResponse = restTemplate.getForEntity(url, Item.class);
        Item item = itemResponse.getBody();
        if (item == null) {
            throw new ResourceNotFoundException("Item", "id", itemId.toString());
        }
        return item;
    }

    private Card getCardById(Long cardId) {
        String url = String.format("%s/%s", cardServiceUrl, cardId);
        ResponseEntity<Card> cardResponse = restTemplate.getForEntity(url, Card.class);
        Card card = cardResponse.getBody();
        if (card == null) {
            throw new ResourceNotFoundException("Card", "id", cardId.toString());
        }
        return card;
    }

    private ShippingMethod getShippingMethodById(Long shippingMethodId) {
        String url = String.format("%s/api/orders/shipping-methods/%s", orderServiceUrl, shippingMethodId);
        ResponseEntity<ShippingMethod> shippingMethodResponse = restTemplate.getForEntity(url, ShippingMethod.class);
        ShippingMethod shippingMethod = shippingMethodResponse.getBody();
        if (shippingMethod == null) {
            throw new ResourceNotFoundException("ShippingMethod", "id", shippingMethodId.toString());
        }
        return shippingMethod;
    }
}
