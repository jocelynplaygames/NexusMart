// File: backend/NexusMart/src/main/java/com/example/NexusMart/validator/ItemInitialQuantityValidator.java
package com.example.NexusMart.validator;

import com.example.NexusMart.annotation.ItemValidInitialQuantity;
import com.example.NexusMart.dto.ItemDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ItemInitialQuantityValidator implements ConstraintValidator<ItemValidInitialQuantity, ItemDTO> {

    @Override
    public void initialize(ItemValidInitialQuantity constraintAnnotation) {
    }

    @Override
    public boolean isValid(ItemDTO itemDTO, ConstraintValidatorContext context) {
        if (itemDTO.getId() == null) {
            return itemDTO.getQuantity() > 0;
        }
        return true;
    }
}

