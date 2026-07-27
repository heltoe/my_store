package com.example.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentKafkaEventListener {
    private final PaymentEventPublisher paymentEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentMarkedSuccess(PaymentMarkedSuccessEvent event) {
        paymentEventPublisher.publishPaymentSucceeded(event.payment());
    }
}
