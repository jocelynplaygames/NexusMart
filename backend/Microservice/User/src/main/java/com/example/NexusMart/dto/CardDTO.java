package com.example.NexusMart.dto;
import com.example.NexusMart.utils.CustomLocalDateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CardDTO {
    private Long id;
    private String userId;
    private String type;
    private String cardNumber;
    private String cardholderName;
    private String billingAddress;

    @JsonDeserialize(using = CustomLocalDateDeserializer.class)
    private LocalDate expirationDate;
}
