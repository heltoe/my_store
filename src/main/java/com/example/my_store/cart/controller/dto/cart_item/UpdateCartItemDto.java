package com.example.my_store.cart.controller.dto.cart_item;

import jakarta.validation.constraints.PositiveOrZero;

/**
 * DTO for {@link com.example.my_store.cart.repository.cart_item.entity.CartItemEntity}
 */
public record UpdateCartItemDto(Long id,
                                @PositiveOrZero(message = "Поле не может быть меньше чем 0") Integer quantity) {
}