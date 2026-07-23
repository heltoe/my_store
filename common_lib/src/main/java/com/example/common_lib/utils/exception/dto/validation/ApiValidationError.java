package com.example.common_lib.utils.exception.dto.validation;

import com.example.common_lib.utils.exception.dto.ApiError;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Schema(description = "Ошибка валидации входных данных")
public class ApiValidationError extends ApiError {
    @Schema(description = "Список ошибок по полям")
    private List<ApiValidationSubError> subErrors;

    public ApiValidationError(HttpStatus statusCode, String errorName, String message) {
        super(statusCode, errorName, message);
        this.subErrors = Collections.emptyList();
    }
}
