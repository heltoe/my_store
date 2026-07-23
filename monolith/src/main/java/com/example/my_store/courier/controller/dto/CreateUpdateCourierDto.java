package com.example.my_store.courier.controller.dto;

import com.example.my_store.courier.repository.entity.CourierEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for {@link CourierEntity}
 */
@Schema(description = "Запрос на создание или обновление курьера")
public record CreateUpdateCourierDto(
        @Schema(description = "Имя курьера", example = "Иван")
        @NotNull(message = "Поле не может быть null") @NotEmpty(message = "Поле не можеть быть пустым") @NotBlank(message = "Поле не можеть быть пустым") String name,
        @Schema(description = "Отчество курьера", example = "Иванович")
        String secondName,
        @Schema(description = "Фамилия курьера", example = "Иванов")
        @NotNull(message = "Поле не может быть null") @NotEmpty(message = "Поле не можеть быть пустым") @NotBlank(message = "Поле не можеть быть пустым") String lastName,
        @Schema(description = "Номер телефона в формате +7XXXXXXXXXX", example = "+79991234567")
        @NotNull(message = "Поле не может быть null") @NotEmpty(message = "Поле не можеть быть пустым") @NotBlank(message = "Поле не можеть быть пустым") @Pattern(message = "Телефон не соотвествует формату", regexp = "\\+7\\d{1,10}") String phoneNumber) {
}
