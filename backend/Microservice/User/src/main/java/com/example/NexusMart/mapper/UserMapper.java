package com.example.NexusMart.mapper;
import com.example.NexusMart.dto.UserDTO;
import com.example.NexusMart.model.Cart;
import com.example.NexusMart.model.User;

public class UserMapper {// UserMapper 类，它就像一个 “数据格式转换器”，专门负责 User（数据库实体）和 UserDTO（数据传输对象）之间的相互转换。

    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO userDTO = new UserDTO();
        //把 User 实体中的基本信息（ID、用户名、邮箱等）一一复制到 UserDTO 中（就像把 Word 里的文字复制到 PDF）。
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setPhoneNumber(user.getPhoneNumber());
        userDTO.setProfilePictureUrl(user.getProfilePictureUrl());
        userDTO.setAddress(AddressMapper.toDTO(user.getAddress()));//调用 AddressMapper 把用户的地址信息（Address 实体）也转成 AddressDTO（嵌套转换，确保所有关联数据都符合传输格式）。
        userDTO.setRating(RatingMapper.toDTO(user.getRating()));
        userDTO.setCartId(user.getCart() != null ? user.getCart().getId() : null); // Set this field
        //只传递购物车的 ID（cartId），而不是完整的购物车对象（Cart），避免传输冗余数据（就像分享文件时只给文件 ID，不给完整内容）。
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

    public static void updateEntityFromDTO(UserDTO userDTO, User user) {//只更新 UserDTO 中携带的新信息（比如用户只改了手机号，就只更新手机号字段）。
        if (userDTO == null || user == null) {
            return;
        }

        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setProfilePictureUrl(userDTO.getProfilePictureUrl());
        if(AddressMapper.toEntity(userDTO.getAddress()) != null){//只有当 UserDTO 中包含地址信息时，才更新用户的地址（避免覆盖原有地址）。
            user.setAddress(AddressMapper.toEntity(userDTO.getAddress()));
        }
        if(RatingMapper.toEntity(userDTO.getRating()) != null){
            user.setRating(RatingMapper.toEntity(userDTO.getRating()));
        }
        if (userDTO.getCartId() != null) {
            Cart cart = new Cart();
            cart.setId(userDTO.getCartId());
            user.setCart(cart);
        }
    }
}




