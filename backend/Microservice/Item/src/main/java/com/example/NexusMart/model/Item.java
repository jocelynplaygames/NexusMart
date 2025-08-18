package com.example.NexusMart.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "items")
public class Item extends BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private String sellerId;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(name = "short_description", nullable = false, length = 500)
    private String shortDescription;

    @Lob
    @Column(name = "long_description", nullable = false, length = 65535)
    private String longDescription;

    @Column(nullable = false)
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private double price;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "sub_image_url1")
    private String subImageUrl1;

    @Column(name = "sub_image_url2")
    private String subImageUrl2;

    @Column(name = "sub_image_url3")
    private String subImageUrl3;

    @Column(name = "sub_image_url4")
    private String subImageUrl4;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String country;

    @ManyToOne
    @JoinColumn(name = "main_category_id", nullable = false)
    private MainCategory mainCategory;

    @ManyToOne
    @JoinColumn(name = "sub_category_id", nullable = false)
    private SubCategory subCategory;

    @Column(name = "rating_id")
    private Long ratingId;

    @Column(name = "date_listed", nullable = false)
    private LocalDateTime dateListed;

    @Column(nullable = false)
    @Min(value = 0, message = "Quantity must be at least 0")
    private int quantity;

    @Column(nullable = false)
    private boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        dateListed = LocalDateTime.now();
    }
}

