package com.example.my_store.payment.controller.dto;

import com.example.my_store.payment.repository.entity.PaymentEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for {@link PaymentEntity}
 */
@Schema(description = "Запрос на создание платежа")
public record CreatePaymentDto(
        @Schema(description = "Идентификатор заказа", example = "1")
        @NotNull(message = "Поле обязательно к заполнению") Long orderId) {
}
