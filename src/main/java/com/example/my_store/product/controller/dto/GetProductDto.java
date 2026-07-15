package com.example.my_store.product.controller.dto;

import com.example.my_store.product.repository.entity.ProductEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * DTO for {@link ProductEntity}
 */
@Schema(description = "Информация о товаре")
public record GetProductDto(
        @Schema(description = "Дата создания") Date createdAt,
        @Schema(description = "Дата обновления") LocalDateTime updatedAt,
        @Schema(description = "Идентификатор товара", example = "1") Long id,
        @Schema(description = "Название товара", example = "Смартфон") String name,
        @Schema(description = "Описание товара", example = "Флагманский смартфон") String description,
        @Schema(description = "Цена товара", example = "999.99") Double price,
        @Schema(description = "Количество на складе", example = "10") Integer quantity,
        @Schema(description = "Признак активности товара", example = "true") Boolean isActive) {
}
