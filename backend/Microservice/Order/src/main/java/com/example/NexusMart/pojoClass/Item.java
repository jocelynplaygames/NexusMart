package com.example.NexusMart.pojoClass;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Item {
    private Long id;
    private String sellerId;
    private String title;
    private String shortDescription;
    private String longDescription;
    private double price;
    private String imageUrl;
    private String subImageUrl1;
    private String subImageUrl2;
    private String subImageUrl3;
    private String subImageUrl4;
    private String city;
    private String state;
    private String country;
    private Long mainCategoryId;
    private Long subCategoryId;
    private Long ratingId;
    private LocalDateTime dateListed;
    private int quantity;
    private boolean deleted;
}
