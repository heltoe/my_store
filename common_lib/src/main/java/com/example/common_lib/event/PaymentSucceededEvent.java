package com.example.common_lib.event;

import java.time.Instant;
import java.util.UUID;

public record PaymentSucceededEvent(
        UUID eventId,
        Long paymentId,
        Long orderId,
        Instant occurredAt
) {
}
