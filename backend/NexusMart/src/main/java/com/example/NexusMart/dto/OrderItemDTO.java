// File: backend/NexusMart/src/main/java/com/example/NexusMart/dto/OrderItemDTO.java
package com.example.NexusMart.dto;
import lombok.Data;

@Data
public class OrderItemDTO {
    private Long id;
    private Long orderId;
    private Long itemId;
    private String itemName;
    private int quantity;
    private double price;
}

