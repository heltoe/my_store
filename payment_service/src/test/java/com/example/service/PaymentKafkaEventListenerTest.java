package com.example.service;

import com.example.repository.entity.PaymentEntity;
import com.example.repository.entity.STATE_PAYMENT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentKafkaEventListenerTest {

    @Mock
    private PaymentEventPublisher paymentEventPublisher;

    @InjectMocks
    private PaymentKafkaEventListener paymentKafkaEventListener;

    @Test
    @DisplayName("after-commit публикует событие в Kafka")
    void onPaymentMarkedSuccess_publishesToKafka() {
        PaymentEntity payment = new PaymentEntity();
        payment.setId(1L);
        payment.setOrderId(2L);
        payment.setStatus(STATE_PAYMENT.SUCCESS);
        PaymentMarkedSuccessEvent event = new PaymentMarkedSuccessEvent(payment);

        paymentKafkaEventListener.onPaymentMarkedSuccess(event);

        verify(paymentEventPublisher).publishPaymentSucceeded(payment);
    }
}
