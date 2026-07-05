package com.example.my_store.order.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * DTO for {@link com.example.my_store.order.repository.order_item.entity.OrderItemEntity}
 */
public record CreateOrderItemDto(
     @NotNull(message = "Поле не может быть null") Long orderOrderId,
     @NotNull(message = "Поле не может быть null") Long productProductId,
     @NotNull(message = "Поле не может быть null") @Positive(message = "Поле не может быть меньше 0") Double price,
     @NotNull(message = "Поле не может быть null") @PositiveOrZero(message = "Поле не может быть меньше 1") Integer quantity
) {
}