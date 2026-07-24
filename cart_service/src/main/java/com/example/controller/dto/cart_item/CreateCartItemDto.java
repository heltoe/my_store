package com.example.controller.dto.cart_item;

import com.example.repository.cart_item.entity.CartItemEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO for {@link CartItemEntity}
 */
@Schema(description = "Запрос на добавление товара в корзину")
public record CreateCartItemDto(
        @Schema(description = "Идентификатор корзины", example = "1")
        @NotNull(message = "Поле не может быть null") Long cart_id,
        @Schema(description = "Идентификатор товара", example = "1")
        @NotNull(message = "Поле не может быть null") Long product_id,
        @Schema(description = "Количество товара", example = "1")
        @NotNull(message = "Поле не может быть null") @Positive(message = "Поле не может быть меньше чем 1") Integer quantity) {
}
