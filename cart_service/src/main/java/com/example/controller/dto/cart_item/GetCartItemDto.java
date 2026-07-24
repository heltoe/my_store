package com.example.controller.dto.cart_item;

import com.example.repository.cart_item.entity.CartItemEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * DTO for {@link CartItemEntity}
 */
@Schema(description = "Позиция корзины")
public record GetCartItemDto(
        @Schema(description = "Дата создания") Date createdAt,
        @Schema(description = "Дата обновления") LocalDateTime updatedAt,
        @Schema(description = "Идентификатор позиции", example = "1") Long id,
        @Schema(description = "Количество товара", example = "2") Integer quantity) {
}
