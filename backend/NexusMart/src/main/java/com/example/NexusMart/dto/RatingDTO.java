// File: backend/NexusMart/src/main/java/com/example/NexusMart/dto/RatingDTO.java
package com.example.NexusMart.dto;
import lombok.Data;

@Data
public class RatingDTO {
    private Long id;
    private String entityId;
    private String entityType;
    private int totalRating;
    private int numRatings;
}
