package com.example.my_store.order.controller.dto;

import com.example.my_store.order.repository.order.entity.STATE_ORDER;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for {@link com.example.my_store.order.repository.order.entity.OrderEntity}
 */
public record ChangeOrderStatusDto(
        @NotNull(message = "Поле не может быть null") STATE_ORDER status,
        @NotNull(message = "Поле не может быть null") Long orderId
) {
}