package com.example.NexusMart.mapper;

import com.example.NexusMart.dto.ItemDTO;
import com.example.NexusMart.model.*;
import org.springframework.stereotype.Component;

@Component
public class ItemMapper {

    public static Item toEntity(ItemDTO itemDTO, MainCategory mainCategory, SubCategory subCategory) {
        Item item = new Item();
        item.setId(itemDTO.getId());
        item.setSellerId(itemDTO.getSellerId());
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
        item.setState(itemDTO.getState());
        item.setCountry(itemDTO.getCountry());
        item.setMainCategory(mainCategory);
        item.setSubCategory(subCategory);
        item.setRatingId(itemDTO.getRatingId());
        item.setQuantity(itemDTO.getQuantity());
        item.setDeleted(itemDTO.isDeleted());
        return item;
    }

    public static ItemDTO toDTO(Item item) {
        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setId(item.getId());
        itemDTO.setSellerId(item.getSellerId());
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
        itemDTO.setState(item.getState());
        itemDTO.setCountry(item.getCountry());
        itemDTO.setMainCategoryId(item.getMainCategory().getId());
        itemDTO.setSubCategoryId(item.getSubCategory().getId());
        itemDTO.setRatingId(item.getRatingId());
        itemDTO.setQuantity(item.getQuantity());
        itemDTO.setDeleted(item.isDeleted());
        return itemDTO;
    }

    public static void updateEntityFromDTO(ItemDTO itemDTO, Item item, MainCategory mainCategory, SubCategory subCategory) {
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
        item.setState(itemDTO.getState());
        item.setCountry(itemDTO.getCountry());
        item.setMainCategory(mainCategory);
        item.setSubCategory(subCategory);
        item.setRatingId(itemDTO.getRatingId());
        item.setQuantity(itemDTO.getQuantity());
        item.setDeleted(itemDTO.isDeleted());
    }
}
