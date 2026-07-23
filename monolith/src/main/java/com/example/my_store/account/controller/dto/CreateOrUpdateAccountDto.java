package com.example.my_store.account.controller.dto;

import com.example.my_store.account.repository.entity.AccountEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for {@link AccountEntity}
 */
@Schema(description = "Запрос на создание или обновление аккаунта")
public record CreateOrUpdateAccountDto(
        @Schema(description = "Номер телефона в формате +7XXXXXXXXXX", example = "+79991234567")
        @NotNull(message = "Поле обязательно к заполнению") @Pattern(message = "Телефон не соотвествует формату", regexp = "\\+7\\d{1,10}") @NotEmpty(message = "Поле не может быть пустым") @NotBlank(message = "Поле не может быть пустым") String phoneNumber,
        @Schema(description = "Имя", example = "Иван")
        @NotNull(message = "Поле обязательно к заполнению") @NotEmpty(message = "Поле не может быть пустым") @NotBlank(message = "Поле не может быть пустым") String firstName,
        @Schema(description = "Отчество", example = "Иванович")
        String secondName,
        @Schema(description = "Фамилия", example = "Иванов")
        @NotNull(message = "Поле обязательно к заполнению") @NotEmpty(message = "Поле не может быть пустым") @NotBlank(message = "Поле не может быть пустым") String lastName) {
}
