package com.example.NexusMart.mapper;
import com.example.NexusMart.dto.UserDTO;
import com.example.NexusMart.model.Cart;
import com.example.NexusMart.model.User;

public class UserMapper {

    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setPhoneNumber(user.getPhoneNumber());
        userDTO.setProfilePictureUrl(user.getProfilePictureUrl());
        userDTO.setAddress(AddressMapper.toDTO(user.getAddress()));
        userDTO.setRating(RatingMapper.toDTO(user.getRating()));
        userDTO.setCartId(user.getCart() != null ? user.getCart().getId() : null); // Set this field
        return userDTO;
    }

    public static User toEntity(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }

        User user = new User();
        user.setId(userDTO.getId());
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setProfilePictureUrl(userDTO.getProfilePictureUrl());
        user.setAddress(AddressMapper.toEntity(userDTO.getAddress()));
        user.setRating(RatingMapper.toEntity(userDTO.getRating()));
        // Find the Cart entity by its ID and set it
        if (userDTO.getCartId() != null) {
            Cart cart = new Cart();
            cart.setId(userDTO.getCartId());
            user.setCart(cart); // Set this field
        }
        return user;
    }

    public static void updateEntityFromDTO(UserDTO userDTO, User user) {
        if (userDTO == null || user == null) {
            return;
        }

        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setProfilePictureUrl(userDTO.getProfilePictureUrl());
        user.setAddress(AddressMapper.toEntity(userDTO.getAddress()));
        user.setRating(RatingMapper.toEntity(userDTO.getRating()));
        if (userDTO.getCartId() != null) {
            Cart cart = new Cart();
            cart.setId(userDTO.getCartId());
            user.setCart(cart); // Set this field
        }
    }
}




