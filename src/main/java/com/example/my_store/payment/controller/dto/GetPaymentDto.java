package com.example.my_store.payment.controller.dto;

import com.example.my_store.payment.repository.entity.STATE_PAYMENT;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * DTO for {@link com.example.my_store.payment.repository.entity.PaymentEntity}
 */
public record GetPaymentDto(Date createdAt, LocalDateTime updatedAt, Long id, Long orderId, STATE_PAYMENT status) {
}