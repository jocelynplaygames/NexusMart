package com.example.NexusMart.pojoClass;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Rating {
    private Long id;
    private String entityId;
    private EntityType entityType;
    private int totalRating;
    private int numRatings;
    private String createdAt;
    private String updatedAt;

    public enum EntityType {
        PRODUCT, SELLER
    }
}