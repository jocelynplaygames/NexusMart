package com.example.NexusMart.dto;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private String userId;
    private String userName;
    private double totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemDTO> orderItems;
    private String cardType;
    private String lastFourDigit;
    private Long shippingMethodId;
    private String shippingMethodName;
    private double shippingCost;
}
