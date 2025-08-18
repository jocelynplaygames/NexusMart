// File: backend/NexusMart/src/main/java/com/example/NexusMart/dto/ResponseDto.java
package com.example.NexusMart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
public class ResponseDto {
    private String statusCode;
    private String message;
    private String token;
    private UserDTO user; // New field for the updated user

    public ResponseDto(String statusCode, String message, String token, UserDTO user) {
        this.statusCode = statusCode;
        this.message = message;
        this.token = token;
        this.user = user;
    }

    // Getters and setters
}
