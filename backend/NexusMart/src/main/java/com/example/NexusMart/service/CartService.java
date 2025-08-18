// File: backend/NexusMart/src/main/java/com/example/NexusMart/service/CartService.java
package com.example.NexusMart.service;

import com.example.NexusMart.dto.CartDTO;
import com.example.NexusMart.dto.CartItemInputDTO;
import com.example.NexusMart.dto.CartItemOutputDTO;
import com.example.NexusMart.exception.ResourceNotFoundException;
import com.example.NexusMart.mapper.CartItemMapper;
import com.example.NexusMart.mapper.CartMapper;
import com.example.NexusMart.model.Cart;
import com.example.NexusMart.model.CartItem;
import com.example.NexusMart.model.Item;
import com.example.NexusMart.model.User;
import com.example.NexusMart.repository.CartItemRepository;
import com.example.NexusMart.repository.ItemRepository;
import com.example.NexusMart.repository.CartRepository;
import com.example.NexusMart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Autowired
    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, UserRepository userRepository, ItemRepository itemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    public CartDTO createCart(CartDTO cartDTO) {
        Cart cart = CartMapper.toEntity(cartDTO, itemRepository);
        cart = cartRepository.save(cart);
        return CartMapper.toDTO(cart, itemRepository);
    }

    public CartDTO getCart(Long id) {
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", id.toString()));
        return CartMapper.toDTO(cart, itemRepository);
    }

    public CartDTO updateCart(CartDTO cartDTO) {
        Cart cart = cartRepository.findById(cartDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartDTO.getId().toString()));

        List<CartItemInputDTO> cartItemInputDTOs = cartDTO.getCartItemsInput().stream()
                .map(itemDTO -> (CartItemInputDTO) itemDTO)
                .collect(Collectors.toList());
        List<CartItem> cartItems = CartItemMapper.toEntityList(cartItemInputDTOs, itemRepository);

        // Clear existing items and add the updated items
        cart.getCartItems().clear();
        for (CartItem cartItem : cartItems) {
            cartItem.setCart(cart);
            cart.getCartItems().add(cartItem);
        }

        cart = cartRepository.save(cart);
        return CartMapper.toDTO(cart, itemRepository);
    }

    public void deleteCartByUserId(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (user.getCart() != null) {
            user.setCart(null);
            userRepository.save(user);
        }

        Cart cart = (Cart) cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));
        cartRepository.delete(cart);
    }

    public CartDTO ensureCartExists(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (user.getCart() == null) {
            Cart cart = new Cart();
            cart.setUser(user);
            cart = cartRepository.save(cart);
            user.setCart(cart);
            userRepository.save(user);
        }
        return CartMapper.toDTO(user.getCart(), itemRepository);
    }

    public CartItemOutputDTO addItemToCart(CartItemInputDTO cartItemDTO) {
        Cart cart = cartRepository.findById(cartItemDTO.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartItemDTO.getCartId().toString()));

        Item item = itemRepository.findById(cartItemDTO.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item", "itemId", cartItemDTO.getItemId().toString()));

        Optional<CartItem> existingCartItem = cart.getCartItems().stream()
                .filter(ci -> ci.getItem().getId().equals(cartItemDTO.getItemId()))
                .findFirst();

        CartItem cartItem;
        if (existingCartItem.isPresent()) {
            cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + cartItemDTO.getQuantity());
        } else {
            cartItem = CartItemMapper.toEntity(cartItemDTO, item);
            cartItem.setCart(cart);
            cart.getCartItems().add(cartItem);
        }

        cartItem = cartItemRepository.save(cartItem);
        return CartItemMapper.toOutputDTO(cartItem, item);
    }
}



