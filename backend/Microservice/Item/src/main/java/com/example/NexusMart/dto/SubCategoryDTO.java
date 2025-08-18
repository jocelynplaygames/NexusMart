package com.example.NexusMart.dto;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class SubCategoryDTO implements Serializable {
    private Long id;
    private Long mainCategoryId;
    private String mainCategoryName;
    private String name;
    private String imageUrl; // Added imageUrl field
    private Timestamp createdAt;
    private Timestamp updatedAt;
}

