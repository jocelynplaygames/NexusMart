package com.example.NexusMart.pojoClass;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Cart {
    private Long id;
    private String userId;
    private List<CartItem> cartItemsInput = new ArrayList<>();
    private List<CartItem> cartItemsOutput = new ArrayList<>();
}