package com.example.service;

import com.example.repository.entity.PaymentEntity;

public record PaymentMarkedSuccessEvent(PaymentEntity payment) {
}
