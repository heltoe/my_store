package com.example.controller.dto;

import com.example.repository.order.entity.OrderEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for {@link OrderEntity}
 */
@Schema(description = "Базовые данные для создания заказа")
public record BaseCreateOrderDto(
        @Schema(description = "Идентификатор аккаунта покупателя", example = "1")
        @NotNull(message = "Поле не может быть null") Long accountId) {
}
