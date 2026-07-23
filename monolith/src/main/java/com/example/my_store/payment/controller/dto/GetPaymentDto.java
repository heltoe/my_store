package com.example.my_store.payment.controller.dto;

import com.example.my_store.payment.repository.entity.STATE_PAYMENT;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * DTO for {@link com.example.my_store.payment.repository.entity.PaymentEntity}
 */
@Schema(description = "Информация о платеже")
public record GetPaymentDto(
        @Schema(description = "Дата создания") Date createdAt,
        @Schema(description = "Дата обновления") LocalDateTime updatedAt,
        @Schema(description = "Идентификатор платежа", example = "1") Long id,
        @Schema(description = "Идентификатор заказа", example = "1") Long orderId,
        @Schema(description = "Статус платежа") STATE_PAYMENT status) {
}
