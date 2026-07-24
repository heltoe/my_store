package com.example.controller.dto;

import com.example.repository.entity.ProductEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * DTO for {@link ProductEntity}
 */
@Schema(description = "Запрос на создание или обновление товара")
public record CreateOrUpdateProductDto(
        @Schema(description = "Название товара", example = "Смартфон")
        @NotBlank(message = "Поле не может быть пустым") String name,
        @Schema(description = "Описание товара", example = "Флагманский смартфон")
        @NotBlank(message = "Поле не может быть пустым") String description,
        @Schema(description = "Цена товара", example = "999.99")
        @NotNull(message = "Поле не может быть null") @Positive(message = "Поле должно быть больше 0") Double price,
        @Schema(description = "Количество на складе", example = "10")
        @NotNull(message = "Поле не может быть null") @PositiveOrZero(message = "Поле не может быть отрицательным") Integer quantity) {
}
