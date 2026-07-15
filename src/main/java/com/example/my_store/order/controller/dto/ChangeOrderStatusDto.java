package com.example.my_store.order.controller.dto;

import com.example.my_store.order.repository.order.entity.STATE_ORDER;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for {@link com.example.my_store.order.repository.order.entity.OrderEntity}
 */
@Schema(description = "Запрос на изменение статуса заказа")
public record ChangeOrderStatusDto(
        @Schema(description = "Новый статус заказа", example = "ACCEPTED")
        @NotNull(message = "Поле не может быть null") STATE_ORDER status
) {
}
