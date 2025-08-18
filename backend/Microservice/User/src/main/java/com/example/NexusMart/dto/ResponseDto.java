package com.example.NexusMart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
public class ResponseDto {
    private String statusCode;
    private String message;
    private String token;
    private UserDTO user; 

    public ResponseDto(String statusCode, String message, String token, UserDTO user) {
        this.statusCode = statusCode;
        this.message = message;
        this.token = token;
        this.user = user;
    }

    
}
