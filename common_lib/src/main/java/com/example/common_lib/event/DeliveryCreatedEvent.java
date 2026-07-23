package com.example.common_lib.event;

import java.time.Instant;
import java.util.UUID;

public record DeliveryCreatedEvent(
        UUID eventId,
        Long deliveryId,
        Long orderId,
        Long courierId,
        Instant occurredAt
) {
}
