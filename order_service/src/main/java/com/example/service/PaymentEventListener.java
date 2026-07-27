package com.example.service;

import com.example.common_lib.dto.STATE_ORDER;
import com.example.common_lib.event.PaymentSucceededEvent;
import com.example.common_lib.kafka.KafkaTopics;
import com.example.common_lib.utils.exception.CommonConflictException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {
    private final OrderService orderService;

    @KafkaListener(topics = KafkaTopics.PAYMENT_SUCCEEDED, groupId = "order-service")
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        try {
            log.info("Received payment.succeeded: orderId=${}, paymentId={}", event.orderId(), event.paymentId());
            orderService.changeOrderState(event.orderId(), STATE_ORDER.PAID);
        } catch (CommonConflictException e) {
            log.warn("Skip duplicate or invalid transition for orderId={}: {}", event.orderId(), e.getMessage());
        }
    }
}
