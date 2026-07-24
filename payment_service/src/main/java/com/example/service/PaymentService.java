package com.example.service;

import com.example.controller.dto.CreatePaymentDto;
import com.example.controller.dto.GetPaymentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentService {
    Page<GetPaymentDto> getAll(Pageable pageable);

    GetPaymentDto getOne(Long id);

    GetPaymentDto getByOrderId(Long orderId);

    List<GetPaymentDto> getMany(List<Long> ids);

    GetPaymentDto create(CreatePaymentDto dto);

    GetPaymentDto markSuccess(Long id);

    GetPaymentDto markFailure(Long id);
}
