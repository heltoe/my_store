package com.example.my_store.payment.controller.dto;

import com.example.my_store.payment.repository.entity.PaymentEntity;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for {@link PaymentEntity}
 */
public record CreateOrUpdatePaymentDto(@NotNull(message = "Поле обязательно к заполнению") Long id) {
}