package com.example.my_store.order.controller.dto;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * DTO for {@link com.example.my_store.order.repository.order.entity.OrderEntity}
 */
public record GetOrderDto(Date createdAt, LocalDateTime updatedAt, Long id) {
}