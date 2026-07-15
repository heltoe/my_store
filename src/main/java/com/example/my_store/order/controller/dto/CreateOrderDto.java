package com.example.my_store.order.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateOrderDto(
        @Valid @NotNull(message = "Поле не может быть null") BaseCreateOrderDto orderDto,
        @Valid @NotEmpty(message = "Список не может быть пустым") List<CreateOrderItemDto> items
) {
}
