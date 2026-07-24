package com.example.common_lib.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Date;

@Schema(description = "Информация о доставке")
public record GetDeliveryDto(
        @Schema(description = "Дата создания") Date createdAt,
        @Schema(description = "Дата обновления") LocalDateTime updatedAt,
        @Schema(description = "Идентификатор доставки", example = "1") Long id,
        @Schema(description = "Идентификатор заказа", example = "1") Long orderId,
        @Schema(description = "Идентификатор курьера", example = "1") Long courierId,
        @Schema(description = "Дата и время доставки") LocalDateTime deliveryDate,
        @Schema(description = "Адрес доставки", example = "ул. Примерная, д. 1") String deliveryPlace,
        @Schema(description = "Дополнительное описание") String description,
        @Schema(description = "Широта", example = "55.7558") Double lat,
        @Schema(description = "Долгота", example = "37.6173") Double lon) {
}
