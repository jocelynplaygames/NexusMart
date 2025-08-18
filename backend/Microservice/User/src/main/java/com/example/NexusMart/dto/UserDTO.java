// File: backend/Microservice/User/src/main/java/com/example/NexusMart/dto/UserDTO.java
package com.example.NexusMart.dto;
import lombok.Data;

@Data
public class UserDTO {
    private String id;
    private String username;
    private String email;
    private String profilePictureUrl;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private AddressDTO address;
    private RatingDTO rating;
    private String password;
    private Long cartId; // New field for cartId
    private String createdAt; // Add these if they are part of your DTO
    private String updatedAt; // Add these if they are part of your DTO
}



