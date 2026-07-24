package com.example.controller.dto;

import com.example.repository.order_item.entity.OrderItemEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO for {@link OrderItemEntity}
 */
@Schema(description = "Позиция заказа")
public record CreateOrderItemDto(
        @Schema(description = "Идентификатор товара", example = "1")
        @NotNull(message = "Поле не может быть null") Long productId,
        @Schema(description = "Количество товара", example = "2")
        @NotNull(message = "Поле не может быть null") @Positive(message = "Поле должно быть больше 0") Integer quantity
) {
}
