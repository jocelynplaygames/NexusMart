package com.example.NexusMart.mapper;

import com.example.NexusMart.dto.ItemDTO;
import com.example.NexusMart.model.*;

public class ItemMapper {

    public static ItemDTO toDTO(Item item) {
        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setId(item.getId());
        itemDTO.setSellerId(item.getSeller().getId());
        itemDTO.setUsername(item.getSeller().getUsername()); // Set seller's username
        itemDTO.setTitle(item.getTitle());
        itemDTO.setShortDescription(item.getShortDescription());
        itemDTO.setLongDescription(item.getLongDescription());
        itemDTO.setPrice(item.getPrice());
        itemDTO.setImageUrl(item.getImageUrl());
        itemDTO.setSubImageUrl1(item.getSubImageUrl1());
        itemDTO.setSubImageUrl2(item.getSubImageUrl2());
        itemDTO.setSubImageUrl3(item.getSubImageUrl3());
        itemDTO.setSubImageUrl4(item.getSubImageUrl4());
        itemDTO.setCity(item.getCity());
        itemDTO.setState(item.getState()); // New field
        itemDTO.setCountry(item.getCountry());
        itemDTO.setMainCategoryId(item.getMainCategory().getId());
        itemDTO.setSubCategoryId(item.getSubCategory().getId());
        itemDTO.setRatingId(item.getRating() != null ? item.getRating().getId() : null);
        itemDTO.setDateListed(item.getDateListed());
        itemDTO.setCreatedAt(item.getCreatedAt());
        itemDTO.setUpdatedAt(item.getUpdatedAt());
        itemDTO.setQuantity(item.getQuantity());
        return itemDTO;
    }

    public static Item toEntity(ItemDTO itemDTO, User seller, MainCategory mainCategory, SubCategory subCategory, Rating rating) {
        Item item = new Item();
        item.setSeller(seller);
        item.setTitle(itemDTO.getTitle());
        item.setShortDescription(itemDTO.getShortDescription());
        item.setLongDescription(itemDTO.getLongDescription());
        item.setPrice(itemDTO.getPrice());
        item.setImageUrl(itemDTO.getImageUrl());
        item.setSubImageUrl1(itemDTO.getSubImageUrl1());
        item.setSubImageUrl2(itemDTO.getSubImageUrl2());
        item.setSubImageUrl3(itemDTO.getSubImageUrl3());
        item.setSubImageUrl4(itemDTO.getSubImageUrl4());
        item.setCity(itemDTO.getCity());
        item.setState(itemDTO.getState()); // New field
        item.setCountry(itemDTO.getCountry());
        item.setMainCategory(mainCategory);
        item.setSubCategory(subCategory);
        item.setRating(rating);
        item.setDateListed(itemDTO.getDateListed());
        item.setQuantity(itemDTO.getQuantity());
        return item;
    }

    public static void updateEntityFromDTO(ItemDTO itemDTO, Item item, MainCategory mainCategory, SubCategory subCategory, Rating rating) {
        item.setTitle(itemDTO.getTitle());
        item.setShortDescription(itemDTO.getShortDescription());
        item.setLongDescription(itemDTO.getLongDescription());
        item.setPrice(itemDTO.getPrice());
        item.setCity(itemDTO.getCity());
        item.setState(itemDTO.getState()); // New field
        item.setCountry(itemDTO.getCountry());
        item.setMainCategory(mainCategory);
        item.setSubCategory(subCategory);
        item.setRating(rating);
        item.setQuantity(itemDTO.getQuantity());
    }
}

