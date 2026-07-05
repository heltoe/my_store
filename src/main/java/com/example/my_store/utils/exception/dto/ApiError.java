package com.example.my_store.utils.exception.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
public class ApiError {
    private final HttpStatus statusCode;
    private final String errorName;
    private final String message;
    private final LocalDateTime dateTime = LocalDateTime.now();
}
