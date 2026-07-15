package com.example.my_store.cart.controller.dto.cart_item;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * DTO for {@link com.example.my_store.cart.repository.cart_item.entity.CartItemEntity}
 */
@Schema(description = "Позиция корзины")
public record GetCartItemDto(
        @Schema(description = "Дата создания") Date createdAt,
        @Schema(description = "Дата обновления") LocalDateTime updatedAt,
        @Schema(description = "Идентификатор позиции", example = "1") Long id,
        @Schema(description = "Количество товара", example = "2") Integer quantity) {
}
