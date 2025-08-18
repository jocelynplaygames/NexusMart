package com.example.NexusMart.dto;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class MainCategoryDTO {
    private Long id;
    private String name;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    // Getters and setters...
}