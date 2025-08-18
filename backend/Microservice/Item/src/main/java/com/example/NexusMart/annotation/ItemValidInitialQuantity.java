package com.example.NexusMart.annotation;

import com.example.NexusMart.validator.ItemInitialQuantityValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ItemInitialQuantityValidator.class)
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ItemValidInitialQuantity {
    String message() default "Quantity must be greater than 0 when creating a new item";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}