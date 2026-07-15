package com.example.my_store.utils.exception.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

@Schema(description = "Ошибка API")
public class BaseApiError extends ApiError {
    public BaseApiError(HttpStatus statusCode, String errorName, String message) {
        super(statusCode, errorName, message);
    }
}
