package com.example.NexusMart.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "shipping_methods")
public class ShippingMethod extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Lob
    private String description;

    @Column(nullable = false)
    private double price;

    @Column(name = "estimated_delivery_time")
    private String estimatedDeliveryTime;

    // Getters and setters are inherited from BaseEntity
}

