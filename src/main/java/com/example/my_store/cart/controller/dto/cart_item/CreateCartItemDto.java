package com.example.my_store.cart.controller.dto.cart_item;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO for {@link com.example.my_store.cart.repository.cart_item.entity.CartItemEntity}
 */
public record CreateCartItemDto(
@NotNull(message = "Поле не может быть null") Long cart_id, 
@NotNull(message = "Поле не может быть null") Long product_id,
@NotNull(message = "Поле не может быть null") @Positive(message = "Поле не может быть меньше чем 1") Integer quantity) {
}