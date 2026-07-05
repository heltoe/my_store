package com.example.my_store.product.controller.dto;

import com.example.my_store.product.repository.entity.ProductEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO for {@link ProductEntity}
 */
public record CreateOrUdpateProductDto(
        @NotNull(message = "Поле не может быть null") @NotEmpty(message = "Поле не может быть пустым") @NotBlank(message = "Поле не может быть пустым") String name,
        @NotNull(message = "Поле не может быть null") @NotEmpty(message = "Поле не может быть пустым") @NotBlank(message = "Поле не может быть пустым") String description,
        @NotNull(message = "Поле не может быть null") @Positive(message = "Поле не может быть меньше нуля") Double price,
        Integer quantity) {
}