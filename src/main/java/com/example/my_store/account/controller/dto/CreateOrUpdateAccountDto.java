package com.example.my_store.account.controller.dto;

import com.example.my_store.account.repository.entity.AccountEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for {@link AccountEntity}
 */
public record CreateOrUpdateAccountDto(
        @NotNull(message = "Поле обязательно к заполнению") @Pattern(message = "Телефон не соотвествует формату", regexp = "\\+7\\d{1,10}") @NotEmpty(message = "Поле не может быть пустым") @NotBlank(message = "Поле не может быть пустым") String phoneNumber,
        @NotNull(message = "Поле обязательно к заполнению") @NotEmpty(message = "Поле не может быть пустым") @NotBlank(message = "Поле не может быть пустым") String firstName,
        String secondName,
        @NotNull(message = "Поле обязательно к заполнению") @NotEmpty(message = "Поле не может быть пустым") @NotBlank(message = "Поле не может быть пустым") String lastName) {
}