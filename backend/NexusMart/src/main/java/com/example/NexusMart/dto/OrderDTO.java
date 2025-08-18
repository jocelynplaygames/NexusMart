// File: backend/NexusMart/src/main/java/com/example/NexusMart/dto/OrderDTO.java
package com.example.NexusMart.dto;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
//OrderDTO（订单数据传输对象），用于封装订单的详细信息
//提供前端需要的完整信息
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
