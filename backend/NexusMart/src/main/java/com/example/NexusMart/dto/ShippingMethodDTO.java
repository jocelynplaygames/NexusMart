// File: backend/NexusMart/src/main/java/com/example/NexusMart/dto/ShippingMethodDTO.java
package com.example.NexusMart.dto;
import lombok.Data;

@Data
public class ShippingMethodDTO {
    private Long id;
    private String name;
    private String description;
    private double price;
    private String estimatedDeliveryTime;
}
