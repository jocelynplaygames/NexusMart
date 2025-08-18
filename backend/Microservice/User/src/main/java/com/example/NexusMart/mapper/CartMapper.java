package com.example.NexusMart.mapper;

import com.example.NexusMart.dto.CartDTO;
import com.example.NexusMart.model.Cart;
import com.example.NexusMart.model.CartItem;
import com.example.NexusMart.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    private static RestTemplate restTemplate;
    private static String itemServiceUrl;

    @Autowired
    public CartMapper(RestTemplate restTemplate, @Value("${itemservice.url}") String itemServiceUrl) {
        CartMapper.restTemplate = restTemplate;
        CartMapper.itemServiceUrl = itemServiceUrl;
    }

    public static Cart toEntity(CartDTO cartDTO) {
        Cart cart = new Cart();
        cart.setId(cartDTO.getId());

        User user = new User();
        user.setId(cartDTO.getUserId());
        cart.setUser(user);

        List<CartItem> cartItems = cartDTO.getCartItemsInput().stream()
                .map(CartItemMapper::toEntity)
                .collect(Collectors.toList());
        cart.setCartItems(cartItems);

        return cart;
    }

    public static CartDTO toDTO(Cart cart) {
        CartDTO cartDTO = new CartDTO();
        cartDTO.setId(cart.getId());
        cartDTO.setUserId(cart.getUser().getId());
        cartDTO.setCartItemsOutput(cart.getCartItems().stream()
                .map(CartItemMapper::toOutputDTO)
                .collect(Collectors.toList()));
        return cartDTO;
    }
}





