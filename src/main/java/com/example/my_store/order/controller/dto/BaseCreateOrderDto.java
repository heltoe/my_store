package com.example.my_store.order.controller.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for {@link com.example.my_store.order.repository.order.entity.OrderEntity}
 */
public record BaseCreateOrderDto(@NotNull(message = "Поле не может быть null") Long accountId) {
}