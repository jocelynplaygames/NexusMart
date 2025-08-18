package com.example.NexusMart.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "items")
public class Item extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sellerId", nullable = false)
    private User seller;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(name = "short_description", nullable = false) // Changed field
    private String shortDescription;

    @Lob
    private String longDescription; // New field

    @Column(nullable = false)
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private double price;

    @Column(nullable = false)
    private String imageUrl;

    private String subImageUrl1;
    private String subImageUrl2;
    private String subImageUrl3;
    private String subImageUrl4;

    @Column(nullable = false)
    private String city; // New field

    @Column(nullable = false)
    private String state; // New field

    @Column(nullable = false)
    private String country; // New field

    @ManyToOne
    @JoinColumn(name = "mainCategoryId", nullable = false)
    private MainCategory mainCategory;

    @ManyToOne
    @JoinColumn(name = "subCategoryId", nullable = false)
    private SubCategory subCategory;

    @OneToOne
    @JoinColumn(name = "ratingId")
    private Rating rating;

    @Column(nullable = false)
    private LocalDateTime dateListed;

    @Column(nullable = false)
    @Min(value = 0, message = "Quantity must be at least 0")
    private int quantity; // New field

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Feedback> feedbacks;

    @Column(nullable = false)
    private boolean deleted = false;

    // Getters and setters are inherited from BaseEntity
    @PrePersist
    protected void onCreate() {
        dateListed = LocalDateTime.now();
    }
}


