package com.example.my_store.order.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO for {@link com.example.my_store.order.repository.order_item.entity.OrderItemEntity}
 */
public record CreateOrderItemDto(
     @NotNull(message = "Поле не может быть null") Long productId,
     @NotNull(message = "Поле не может быть null") @Positive(message = "Поле должно быть больше 0") Integer quantity
) {
}