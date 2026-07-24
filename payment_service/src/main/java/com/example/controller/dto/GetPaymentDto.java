package com.example.controller.dto;

import com.example.repository.entity.STATE_PAYMENT;
import com.example.repository.entity.PaymentEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * DTO for {@link PaymentEntity}
 */
@Schema(description = "Информация о платеже")
public record GetPaymentDto(
        @Schema(description = "Дата создания") Date createdAt,
        @Schema(description = "Дата обновления") LocalDateTime updatedAt,
        @Schema(description = "Идентификатор платежа", example = "1") Long id,
        @Schema(description = "Идентификатор заказа", example = "1") Long orderId,
        @Schema(description = "Статус платежа") STATE_PAYMENT status) {
}
