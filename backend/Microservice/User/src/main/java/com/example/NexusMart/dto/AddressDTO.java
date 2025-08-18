package com.example.NexusMart.dto;
import lombok.Data;

@Data
public class AddressDTO {
    private Long id;
    private String userId;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}
