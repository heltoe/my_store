package com.example.my_store.cart.controller.dto.cart;

import com.example.my_store.cart.repository.cart.entity.CartEntity;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for {@link CartEntity}
 */
public record CreateCartDto(@NotNull(message = "Поле не может быть null") Long account_id) {
}