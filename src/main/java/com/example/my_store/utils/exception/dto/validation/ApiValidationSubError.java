package com.example.my_store.utils.exception.dto.validation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ошибка валидации конкретного поля")
public record ApiValidationSubError(
        @Schema(description = "Имя поля", example = "name")
        String field,
        @Schema(description = "Отклонённое значение", example = "")
        Object rejectedValue,
        @Schema(description = "Сообщение об ошибке", example = "Поле не может быть пустым")
        String message
) {
}
