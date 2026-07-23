package com.example.my_store.courier.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * DTO for {@link com.example.my_store.courier.repository.entity.CourierEntity}
 */
@Schema(description = "Информация о курьере")
public record GetCourierDto(
        @Schema(description = "Дата создания") Date createdAt,
        @Schema(description = "Дата обновления") LocalDateTime updatedAt,
        @Schema(description = "Идентификатор курьера", example = "1") Long id,
        @Schema(description = "Имя", example = "Иван") String name,
        @Schema(description = "Отчество", example = "Иванович") String secondName,
        @Schema(description = "Фамилия", example = "Иванов") String lastName,
        @Schema(description = "Номер телефона", example = "+79991234567") String phoneNumber,
        @Schema(description = "Признак активности", example = "true") Boolean isActive) {
}
