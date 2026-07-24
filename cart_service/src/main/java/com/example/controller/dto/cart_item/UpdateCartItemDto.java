package com.example.controller.dto.cart_item;

import com.example.repository.cart_item.entity.CartItemEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * DTO for {@link CartItemEntity}
 */
@Schema(description = "Запрос на изменение количества товара в корзине")
public record UpdateCartItemDto(
        @Schema(description = "Идентификатор позиции корзины", example = "1")
        @NotNull(message = "Поле не может быть null") Long id,
        @Schema(description = "Новое количество товара", example = "2")
        @NotNull(message = "Поле не может быть null")
        @PositiveOrZero(message = "Поле не может быть меньше чем 0") Integer quantity) {
}
