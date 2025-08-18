package com.example.NexusMart.pojoClass;

import lombok.Data;


@Data
public class Item {
    private Long id;
    private String title;
    private double price;
    private String sellerId;
    private String imageUrl;
    private boolean deleted;
    
}
