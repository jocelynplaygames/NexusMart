package com.example.NexusMart.mapper;
import com.example.NexusMart.dto.CartItemInputDTO;
import com.example.NexusMart.dto.CartItemOutputDTO;
import com.example.NexusMart.exception.ResourceNotFoundException;
import com.example.NexusMart.model.Cart;
import com.example.NexusMart.model.CartItem;
import com.example.NexusMart.model.Item;
import com.example.NexusMart.repository.ItemRepository;

import java.util.List;
import java.util.stream.Collectors;

public class CartItemMapper {
    public static CartItem toEntity(CartItemInputDTO dto, Item item) {
        CartItem entity = new CartItem();
        entity.setId(dto.getId());

        Cart cart = new Cart();
        cart.setId(dto.getCartId());
        entity.setCart(cart);

        entity.setItem(item);
        entity.setQuantity(dto.getQuantity());
        return entity;
    }

    public static CartItemOutputDTO toOutputDTO(CartItem entity, Item item) {
        CartItemOutputDTO dto = new CartItemOutputDTO();
        dto.setId(entity.getId());
        dto.setCartId(entity.getCart().getId());
        dto.setItemId(entity.getItem().getId());
        dto.setQuantity(entity.getQuantity());
        dto.setTitle(item.getTitle());
        dto.setImageUrl(item.getImageUrl());
        dto.setPrice(item.getPrice()); // Query price from item
        return dto;
    }

    public static List<CartItem> toEntityList(List<CartItemInputDTO> dtoList, ItemRepository itemRepository) {
        return dtoList.stream().map(dto -> {
            Item item = itemRepository.findById(dto.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item", "id", dto.getItemId().toString()));
            return toEntity(dto, item);
        }).collect(Collectors.toList());
    }

    public static List<CartItemOutputDTO> toOutputDTOList(List<CartItem> entityList, ItemRepository itemRepository) {
        return entityList.stream().map(entity -> {
            Item item = itemRepository.findById(entity.getItem().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item", "id", entity.getItem().getId().toString()));
            return toOutputDTO(entity, item);
        }).collect(Collectors.toList());
    }
}



