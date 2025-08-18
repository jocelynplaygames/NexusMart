package com.example.NexusMart.mapper;

import com.example.NexusMart.dto.OrderDTO;
import com.example.NexusMart.dto.OrderItemDTO;
import com.example.NexusMart.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderDTO toDTO(Order order) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(order.getId());
        orderDTO.setUserId(order.getUser().getId());
        orderDTO.setUserName(order.getUser().getUsername());
        orderDTO.setTotalAmount(order.getTotalAmount());
        orderDTO.setStatus(order.getStatus());
        orderDTO.setCreatedAt(order.getCreatedAt());
        orderDTO.setUpdatedAt(order.getUpdatedAt());
        orderDTO.setCardType(order.getCardType());
        orderDTO.setLastFourDigit(order.getLastFourDigit());
        orderDTO.setShippingMethodId(order.getShippingMethod().getId());
        orderDTO.setShippingMethodName(order.getShippingMethod().getName());
        orderDTO.setShippingCost(order.getShippingMethod().getPrice()); // Include shipping cost

        // Convert OrderItems to OrderItemDTOs
        List<OrderItemDTO> orderItems = order.getOrderItems().stream()
                .map(this::toOrderItemDTO)
                .collect(Collectors.toList());
        orderDTO.setOrderItems(orderItems);

        return orderDTO;
    }

    private OrderItemDTO toOrderItemDTO(OrderItem orderItem) {
        OrderItemDTO orderItemDTO = new OrderItemDTO();
        orderItemDTO.setItemId(orderItem.getItem().getId());
        orderItemDTO.setQuantity(orderItem.getQuantity());
        orderItemDTO.setPrice(orderItem.getPrice());
        orderItemDTO.setItemName(orderItem.getItem().getTitle());
        return orderItemDTO;
    }

    public Order toEntity(OrderDTO orderDTO, User user, ShippingMethod shippingMethod, Card card) {
        Order order = new Order();
        order.setId(orderDTO.getId());
        order.setUser(user);
        order.setTotalAmount(orderDTO.getTotalAmount());
        order.setStatus(orderDTO.getStatus());
        order.setCardType(card.getType());
        order.setLastFourDigit(card.getCardNumber().substring(card.getCardNumber().length() - 4));
        order.setShippingMethod(shippingMethod);

        // Convert OrderItemDTOs to OrderItems
        List<OrderItem> orderItems = orderDTO.getOrderItems().stream()
                .map(orderItemDTO -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    Item newItem = new Item();
                    newItem.setId(orderItemDTO.getItemId());
                    orderItem.setItem(newItem);
                    orderItem.setQuantity(orderItemDTO.getQuantity());
                    orderItem.setPrice(orderItemDTO.getPrice());
                    return orderItem;
                })
                .collect(Collectors.toList());
        order.setOrderItems(orderItems);

        return order;
    }
}