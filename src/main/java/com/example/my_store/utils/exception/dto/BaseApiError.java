package com.example.my_store.utils.exception.dto;

import org.springframework.http.HttpStatus;

public class BaseApiError extends ApiError {
    public BaseApiError(HttpStatus statusCode, String errorName, String message) {
        super(statusCode, errorName, message);
    }
}
