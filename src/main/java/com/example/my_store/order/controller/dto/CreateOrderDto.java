package com.example.my_store.order.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Запрос на создание заказа")
public record CreateOrderDto(
        @Schema(description = "Данные заказа")
        @Valid @NotNull(message = "Поле не может быть null") BaseCreateOrderDto orderDto,
        @Schema(description = "Позиции заказа")
        @Valid @NotEmpty(message = "Список не может быть пустым") List<CreateOrderItemDto> items
) {
}
