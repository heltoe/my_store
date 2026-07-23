package com.example.my_store.order.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for {@link com.example.my_store.order.repository.order.entity.OrderEntity}
 */
@Schema(description = "Базовые данные для создания заказа")
public record BaseCreateOrderDto(
        @Schema(description = "Идентификатор аккаунта покупателя", example = "1")
        @NotNull(message = "Поле не может быть null") Long accountId) {
}
