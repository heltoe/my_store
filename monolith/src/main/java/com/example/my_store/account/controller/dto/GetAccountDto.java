package com.example.my_store.account.controller.dto;

import com.example.my_store.account.repository.entity.AccountEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * DTO for {@link AccountEntity}
 */
@Schema(description = "Информация об аккаунте")
public record GetAccountDto(
        @Schema(description = "Дата создания") Date createdAt,
        @Schema(description = "Дата обновления") LocalDateTime updatedAt,
        @Schema(description = "Идентификатор аккаунта", example = "1") Long id,
        @Schema(description = "Номер телефона", example = "+79991234567") String phoneNumber,
        @Schema(description = "Имя", example = "Иван") String firstName,
        @Schema(description = "Отчество", example = "Иванович") String secondName,
        @Schema(description = "Фамилия", example = "Иванов") String lastName) {
}
