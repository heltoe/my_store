package com.example.my_store.payment.repository.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статус платежа")
public enum STATE_PAYMENT {
    @Schema(description = "Ожидает оплаты")
    PENDING,
    @Schema(description = "Успешно оплачен")
    SUCCESS,
    @Schema(description = "Оплата не прошла")
    FAILURE,
}
