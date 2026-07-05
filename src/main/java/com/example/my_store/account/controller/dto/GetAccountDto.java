package com.example.my_store.account.controller.dto;

import com.example.my_store.account.repository.entity.AccountEntity;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * DTO for {@link AccountEntity}
 */
public record GetAccountDto(Date createdAt, LocalDateTime updatedAt, Long id, String phoneNumber, String firstName,
                            String secondName, String lastName) {
}