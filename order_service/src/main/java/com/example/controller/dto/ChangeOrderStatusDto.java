package com.example.controller.dto;

import com.example.common_lib.dto.STATE_ORDER;
import com.example.repository.order.entity.OrderEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for {@link OrderEntity}
 */
@Schema(description = "Запрос на изменение статуса заказа")
public record ChangeOrderStatusDto(
        @Schema(description = "Новый статус заказа", example = "ACCEPTED")
        @NotNull(message = "Поле не может быть null") STATE_ORDER status
) {
}
