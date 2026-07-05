package com.example.my_store.utils.exception.dto.validation;

import com.example.my_store.utils.exception.dto.ApiError;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class ApiValidationError extends ApiError {
    private List<ApiValidationSubError> subErrors;

    public ApiValidationError(HttpStatus statusCode, String errorName, String message) {
        super(statusCode, errorName, message);
        this.subErrors = Collections.emptyList();
    }
}
