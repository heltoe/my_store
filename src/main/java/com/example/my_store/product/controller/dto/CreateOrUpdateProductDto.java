package com.example.my_store.product.controller.dto;

import com.example.my_store.product.repository.entity.ProductEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * DTO for {@link ProductEntity}
 */
public record CreateOrUpdateProductDto(
        @NotBlank(message = "Поле не может быть пустым") String name,
        @NotBlank(message = "Поле не может быть пустым") String description,
        @NotNull(message = "Поле не может быть null") @Positive(message = "Поле должно быть больше 0") Double price,
        @NotNull(message = "Поле не может быть null") @PositiveOrZero(message = "Поле не может быть отрицательным") Integer quantity) {
}