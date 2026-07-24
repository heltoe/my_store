package com.example.controller.dto.cart;

import com.example.repository.cart.entity.CartEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for {@link CartEntity}
 */
@Schema(description = "Запрос на создание корзины")
public record CreateCartDto(
        @Schema(description = "Идентификатор аккаунта", example = "1")
        @NotNull(message = "Поле не может быть null") Long account_id) {
}
