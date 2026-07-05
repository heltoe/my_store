package com.example.my_store.cart.controller.dto.cart;

import com.example.my_store.cart.repository.cart.entity.CartEntity;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * DTO for {@link CartEntity}
 */
public record GetCartDto(Date createdAt, LocalDateTime updatedAt, Long id) {
}