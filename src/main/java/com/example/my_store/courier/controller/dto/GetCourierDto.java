package com.example.my_store.courier.controller.dto;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * DTO for {@link com.example.my_store.courier.repository.entity.CourierEntity}
 */
public record GetCourierDto(Date createdAt, LocalDateTime updatedAt, Long id, String name, String secondName,
                            String lastName, String phoneNumber, Boolean isActive) {
}