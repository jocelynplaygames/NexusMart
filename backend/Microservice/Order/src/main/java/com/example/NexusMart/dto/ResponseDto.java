package com.example.NexusMart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
public class ResponseDto {
    private String statusCode;
    private String message;
    private String token;

    public ResponseDto(String statusCode, String message, String token) {
        this.statusCode = statusCode;
        this.message = message;
        this.token = token;
    }

    // Getters and setters
}
