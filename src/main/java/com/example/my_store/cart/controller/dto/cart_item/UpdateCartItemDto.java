package com.example.my_store.cart.controller.dto.cart_item;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for {@link com.example.my_store.cart.repository.cart_item.entity.CartItemEntity}
 */
public record UpdateCartItemDto(@NotNull(message = "Поле не может быть null") Long id,
                                @NotNull(message = "Поле не может быть null")
                                @PositiveOrZero(message = "Поле не может быть меньше чем 0") Integer quantity) {
}