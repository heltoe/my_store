package com.example.my_store.cart.controller.dto.cart_item;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * DTO for {@link com.example.my_store.cart.repository.cart_item.entity.CartItemEntity}
 */
public record GetCartItemDto(Date createdAt, LocalDateTime updatedAt, Long id, Integer quantity) {
}