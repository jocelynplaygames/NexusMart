// File: backend/NexusMart/src/main/java/com/example/NexusMart/dto/SubCategoryDTO.java
package com.example.NexusMart.dto;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class SubCategoryDTO {
    private Long id;
    private Long mainCategoryId;
    private String mainCategoryName;
    private String name;
    private String imageUrl; // Added imageUrl field
    private Timestamp createdAt;
    private Timestamp updatedAt;
}

