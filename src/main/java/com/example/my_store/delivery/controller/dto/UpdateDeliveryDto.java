package com.example.my_store.delivery.controller.dto;

import com.example.my_store.delivery.repository.entity.DeliveryEntity;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/**
 * DTO for {@link DeliveryEntity}
 */
public record UpdateDeliveryDto(@NotNull(message = "Поле не может быть null") Long courierId,
                                @NotNull(message = "Поле не может быть null") @Future(message = "Дата не может быть позже текущей") LocalDateTime deliveryDate,
                                @NotNull(message = "Поле не может быть null") @NotEmpty(message = "Поле не может быть пустым") @NotBlank(message = "Поле не может быть пустым") String deliveryPlace,
                                String description,
                                @NotNull(message = "Поле не может быть null") @Min(message = "Не может быть меньше -90", value = -90) @Max(message = "Не может быть больше 90", value = 90) Double lat,
                                @NotNull(message = "Поле не может быть null") @Min(message = "Не может быть меньше -180", value = -180) @Max(message = "Не может быть больше 180", value = 180) Double lon) {
}