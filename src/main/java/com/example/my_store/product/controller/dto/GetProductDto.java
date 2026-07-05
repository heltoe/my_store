package com.example.my_store.product.controller.dto;

import com.example.my_store.product.repository.entity.ProductEntity;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * DTO for {@link ProductEntity}
 */
public record GetProductDto(Date createdAt, LocalDateTime updatedAt, Long id, String name, String description,
                            Double price, Integer quantity) {
}