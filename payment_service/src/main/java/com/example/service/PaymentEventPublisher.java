package com.example.service;

import com.example.common_lib.event.PaymentSucceededEvent;
import com.example.common_lib.kafka.KafkaTopics;
import com.example.repository.entity.PaymentEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentEventPublisher {
    private final KafkaTemplate<String, PaymentSucceededEvent> kafkaTemplate;

    public void publishPaymentSucceeded(PaymentEntity payment) {
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                UUID.randomUUID(),
                payment.getId(),
                payment.getOrderId(),
                Instant.now()
        );
        kafkaTemplate.send(
                KafkaTopics.PAYMENT_SUCCEEDED,
                String.valueOf(payment.getOrderId()),
                event
        );
    }
}
