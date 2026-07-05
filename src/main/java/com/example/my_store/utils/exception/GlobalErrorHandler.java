package com.example.my_store.utils.exception;

import com.example.my_store.utils.exception.dto.ApiError;
import com.example.my_store.utils.exception.dto.BaseApiError;
import com.example.my_store.utils.exception.dto.validation.ApiValidationError;
import com.example.my_store.utils.exception.dto.validation.ApiValidationSubError;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalErrorHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);

    // Обработка ошибки ненайденного ресурса (404 Entity not found Error)
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException ex) {
        log.error("EntityNotFound error: {}", ex.getMessage(), ex);
        BaseApiError error = new BaseApiError(
                HttpStatus.NOT_FOUND,
                "Entity not found",
                ex.getMessage()
        );
        return buildResponseEntity(error);
    }

    // Обработка клиентских ошибок (400 Bad request Error)
    @ExceptionHandler(exception = {
            IllegalArgumentException.class,
            IllegalStateException.class,
            DataIntegrityViolationException.class
    })
    public ResponseEntity<Object> handleBadRequest(Exception ex) {
        log.error("Validation error: {}", ex.getMessage(), ex);
        BaseApiError error = new BaseApiError(
                HttpStatus.BAD_REQUEST,
                "Bad request",
                ex.getMessage()
        );
        return buildResponseEntity(error);
    }

    // Обработка клиентских ошибок (400 Bad request Error)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage(), ex);
        ApiValidationError error = new ApiValidationError(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Validation failed");
        // Собираем под-ошибки из объектов FieldError
        List<ApiValidationSubError> subErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> new ApiValidationSubError(f.getField(),
                        f.getRejectedValue(), f.getDefaultMessage()))
                .collect(Collectors.toList());
        error.setSubErrors(subErrors);
        return buildResponseEntity(error);
    }

    // Обработка всех остальных ошибок (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {
        log.error("Internal error: {}", ex.getMessage(), ex);
        BaseApiError error = new BaseApiError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "Произошла непредвиденная ошибка. Обратитесь в поддержку."
        );
        return buildResponseEntity(error);
    }

    private ResponseEntity<Object> buildResponseEntity(ApiError error) {
        return ResponseEntity.status(error.getStatusCode()).body(error);
    }
}
