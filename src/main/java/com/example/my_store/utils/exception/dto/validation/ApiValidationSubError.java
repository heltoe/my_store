package com.example.my_store.utils.exception.dto.validation;

public record ApiValidationSubError(String field, Object rejectedValue, String message) {
}
