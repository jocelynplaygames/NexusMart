package com.example.NexusMart.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
@Entity
@Table(name = "ratings")
public class Rating extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private EntityType entityType;

    @Column(name = "total_rating", nullable = false)
    private int totalRating;

    @Column(name = "num_ratings", nullable = false)
    private int numRatings;

    public enum EntityType {
        PRODUCT, SELLER
    }

    public Rating() {}

    public Rating(String entityId, EntityType entityType) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.totalRating = 0;
        this.numRatings = 0;
    }
}
