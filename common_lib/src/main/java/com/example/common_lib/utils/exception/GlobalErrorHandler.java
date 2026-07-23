package com.example.common_lib.utils.exception;

import com.example.common_lib.utils.exception.dto.ApiError;
import com.example.common_lib.utils.exception.dto.BaseApiError;
import com.example.common_lib.utils.exception.dto.validation.ApiValidationError;
import com.example.common_lib.utils.exception.dto.validation.ApiValidationSubError;
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

    private ResponseEntity<Object> buildResponseEntity(Exception ex, HttpStatus status, String customMessageError, String logTitle) {
        log.error(logTitle, ex.getMessage(), ex);
        BaseApiError error = new BaseApiError(
                status,
                customMessageError,
                ex.getMessage()
        );
        return buildResponseEntity(error);
    }

    @ExceptionHandler(CommonEntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFound(Exception ex) {
        return buildResponseEntity(ex, HttpStatus.NOT_FOUND, "Entity not found", "EntityNotFound error");
    }

    @ExceptionHandler(CommonConflictException.class)
    public ResponseEntity<Object> handleConflict(Exception ex) {
        return buildResponseEntity(ex, HttpStatus.CONFLICT, "Conflict error", "Conflict error");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        if (isDuplicateProductNameViolation(ex)) {
            return buildResponseEntity(ex, HttpStatus.CONFLICT, "Conflict error", "Conflict error");
        }
        return buildResponseEntity(ex, HttpStatus.BAD_REQUEST, "Bad request", "Validation error");
    }

    @ExceptionHandler(exception = {
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    public ResponseEntity<Object> handleBadRequest(Exception ex) {
        return buildResponseEntity(ex, HttpStatus.BAD_REQUEST, "Bad request", "Validation error");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage(), ex);
        ApiValidationError error = new ApiValidationError(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Validation failed");
        List<ApiValidationSubError> subErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> new ApiValidationSubError(f.getField(),
                        f.getRejectedValue(), f.getDefaultMessage()))
                .collect(Collectors.toList());
        error.setSubErrors(subErrors);
        return buildResponseEntity(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {
        return buildResponseEntity(ex, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Internal error");
    }

    private ResponseEntity<Object> buildResponseEntity(ApiError error) {
        return ResponseEntity.status(error.getStatusCode()).body(error);
    }

    private boolean isDuplicateProductNameViolation(DataIntegrityViolationException ex) {
        Throwable cause = ex.getMostSpecificCause();
        if (cause == null || cause.getMessage() == null) {
            return false;
        }
        String message = cause.getMessage().toLowerCase();
        return message.contains("normalized_name");
    }
}
