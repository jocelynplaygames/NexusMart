package com.example.NexusMart.controller;

import com.example.NexusMart.dto.CreateOrderRequestDTO;
import com.example.NexusMart.dto.OrderDTO;
import com.example.NexusMart.exception.ResourceNotFoundException;
import com.example.NexusMart.model.ShippingMethod;
import com.example.NexusMart.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/shipping-methods")
    public ResponseEntity<List<ShippingMethod>> getShippingMethods() {
        List<ShippingMethod> shippingMethods = orderService.getShippingMethods();
        return ResponseEntity.ok(shippingMethods);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long orderId) {
        OrderDTO orderDTO = orderService.getOrder(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId.toString()));
        return ResponseEntity.ok(orderDTO);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByUser(@PathVariable String userId) {
        List<OrderDTO> orders = orderService.getOrdersByUser(userId);
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody CreateOrderRequestDTO request) {
        Long orderId = orderService.createOrder(request.getCartId(), request.getShippingMethodId(), request.getCardId());
        return ResponseEntity.ok(orderId);
    }
}

