package com.example.my_store.delivery.controller.dto;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * DTO for {@link com.example.my_store.delivery.repository.entity.DeliveryEntity}
 */
public record GetDeliveryDto(Date createdAt, LocalDateTime updatedAt, Long id, Long orderId, Long courierId,
                             LocalDateTime deliveryDate, String deliveryPlace, String description, Double lat,
                             Double lon) {
}