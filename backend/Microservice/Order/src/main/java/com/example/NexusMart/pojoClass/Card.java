package com.example.NexusMart.pojoClass;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class Card {
    private Long id;
    private String userId;
    private String type;
    private String cardNumber;
    private LocalDate expirationDate;
    private String cardholderName;
    private String billingAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
