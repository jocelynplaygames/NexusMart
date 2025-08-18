package com.example.NexusMart.dto;

import lombok.Data;

@Data
public class CreateOrderRequestDTO {
    private Long cartId;
    private Long shippingMethodId;
    private Long cardId;
}
