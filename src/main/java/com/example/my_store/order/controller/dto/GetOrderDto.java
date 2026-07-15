package com.example.my_store.order.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * DTO for {@link com.example.my_store.order.repository.order.entity.OrderEntity}
 */
@Schema(description = "Информация о заказе")
public record GetOrderDto(
        @Schema(description = "Дата создания") Date createdAt,
        @Schema(description = "Дата обновления") LocalDateTime updatedAt,
        @Schema(description = "Идентификатор заказа", example = "1") Long id) {
}
