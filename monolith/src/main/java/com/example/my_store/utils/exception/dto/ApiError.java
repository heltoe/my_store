package com.example.my_store.utils.exception.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
@Schema(description = "Базовая структура ошибки API")
public class ApiError {
    @Schema(description = "HTTP-статус", example = "NOT_FOUND")
    private final HttpStatus statusCode;
    @Schema(description = "Код ошибки", example = "Entity not found")
    private final String errorName;
    @Schema(description = "Сообщение об ошибке", example = "Product with id 1 not found")
    private final String message;
    @Schema(description = "Время возникновения ошибки", example = "2026-07-15T14:00:00")
    private final LocalDateTime dateTime = LocalDateTime.now();
}
