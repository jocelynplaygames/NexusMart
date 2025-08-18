package com.example.NexusMart.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RatingDTO implements Serializable {
    private Long id;
    private String entityId;
    private String entityType;
    private int totalRating;
    private int numRatings;
}
