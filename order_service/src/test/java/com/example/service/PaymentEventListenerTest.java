package com.example.service;

import com.example.common_lib.dto.STATE_ORDER;
import com.example.common_lib.event.PaymentSucceededEvent;
import com.example.common_lib.utils.exception.CommonConflictException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentEventListenerTest {

    private static final Long ORDER_ID = 2L;
    private static final Long PAYMENT_ID = 1L;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private PaymentEventListener paymentEventListener;

    @Test
    @DisplayName("consume переводит заказ в статус PAID")
    void onPaymentSucceeded_changesOrderStateToPaid() {
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                UUID.randomUUID(),
                PAYMENT_ID,
                ORDER_ID,
                Instant.now()
        );

        paymentEventListener.onPaymentSucceeded(event);

        verify(orderService).changeOrderState(ORDER_ID, STATE_ORDER.PAID);
    }

    @Test
    @DisplayName("consume игнорирует повторный или некорректный переход статуса")
    void onPaymentSucceeded_whenConflict_skipsInvalidTransition() {
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                UUID.randomUUID(),
                PAYMENT_ID,
                ORDER_ID,
                Instant.now()
        );
        doThrow(new CommonConflictException("Order status must be `ACCEPTED`"))
                .when(orderService).changeOrderState(ORDER_ID, STATE_ORDER.PAID);

        paymentEventListener.onPaymentSucceeded(event);

        verify(orderService).changeOrderState(ORDER_ID, STATE_ORDER.PAID);
    }
}
