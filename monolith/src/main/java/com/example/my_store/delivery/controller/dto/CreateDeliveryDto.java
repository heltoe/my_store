package com.example.my_store.delivery.controller.dto;

import com.example.my_store.delivery.repository.entity.DeliveryEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/**
 * DTO for {@link DeliveryEntity}
 */
@Schema(description = "Запрос на создание доставки")
public record CreateDeliveryDto(
        @Schema(description = "Идентификатор заказа", example = "1")
        @NotNull(message = "Поле не может быть null") Long orderId,
        @Schema(description = "Идентификатор курьера", example = "1")
        @NotNull(message = "Поле не может быть null") Long courierId,
        @Schema(description = "Планируемая дата и время доставки", example = "2026-07-20T12:00:00")
        @NotNull(message = "Поле не может быть null") @Future(message = "Дата не может быть позже текущей") LocalDateTime deliveryDate,
        @Schema(description = "Адрес доставки", example = "ул. Примерная, д. 1")
        @NotNull(message = "Поле не может быть null") @NotEmpty(message = "Поле не может быть пустым") @NotBlank(message = "Поле не может быть пустым") String deliveryPlace,
        @Schema(description = "Дополнительное описание", example = "Позвонить за 10 минут")
        String description,
        @Schema(description = "Широта", example = "55.7558")
        @NotNull(message = "Поле не может быть null") @Min(message = "Не может быть меньше -90", value = -90) @Max(message = "Не может быть больше 90", value = 90) Double lat,
        @Schema(description = "Долгота", example = "37.6173")
        @NotNull(message = "Поле не может быть null") @Min(message = "Не может быть меньше -180", value = -180) @Max(message = "Не может быть больше 180", value = 180) Double lon) {
}
