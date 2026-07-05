package com.example.my_store.payment.service;

import com.example.my_store.payment.controller.dto.CreateOrUpdatePaymentDto;
import com.example.my_store.payment.controller.dto.GetPaymentDto;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

public interface PaymentService {
    Page<GetPaymentDto> getAll(Pageable pageable);

    GetPaymentDto getOne(Long id);

    List<GetPaymentDto> getMany(List<Long> ids);

    GetPaymentDto create(CreateOrUpdatePaymentDto dto);

    GetPaymentDto patch(Long id);
}
