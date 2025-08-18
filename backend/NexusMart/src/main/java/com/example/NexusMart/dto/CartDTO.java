package com.example.NexusMart.dto;
import lombok.Data;
import java.util.List;

@Data
public class CartDTO {
    private Long id;
    private String userId;
    private List<CartItemInputDTO> cartItemsInput;
    private List<CartItemOutputDTO> cartItemsOutput;
}
