package com.example.my_store.courier.controller.dto;

import com.example.my_store.courier.repository.entity.CourierEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for {@link CourierEntity}
 */
public record CreateUpdateCourierDto(
        @NotNull(message = "Поле не может быть null") @NotEmpty(message = "Поле не можеть быть пустым") @NotBlank(message = "Поле не можеть быть пустым") String name,
        String secondName,
        @NotNull(message = "Поле не может быть null") @NotEmpty(message = "Поле не можеть быть пустым") @NotBlank(message = "Поле не можеть быть пустым") String lastName,
        @NotNull(message = "Поле не может быть null") @NotEmpty(message = "Поле не можеть быть пустым") @NotBlank(message = "Поле не можеть быть пустым") @Pattern(message = "Телефон не соотвествует формату", regexp = "\\+7\\d{1,10}") String phoneNumber) {
}