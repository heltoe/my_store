package com.example.common_lib.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Date;

@Schema(description = "Информация о заказе")
public record GetOrderDto(
        @Schema(description = "Дата создания") Date createdAt,
        @Schema(description = "Дата обновления") LocalDateTime updatedAt,
        @Schema(description = "Идентификатор заказа", example = "1") Long id,
        @Schema(description = "Статус заказа") STATE_ORDER stateOrder
        ) {
}
