package com.example.my_store.cart.controller.dto.cart;

import com.example.my_store.cart.repository.cart.entity.CartEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * DTO for {@link CartEntity}
 */
@Schema(description = "Информация о корзине")
public record GetCartDto(
        @Schema(description = "Дата создания") Date createdAt,
        @Schema(description = "Дата обновления") LocalDateTime updatedAt,
        @Schema(description = "Идентификатор корзины", example = "1") Long id) {
}
