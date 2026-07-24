package com.example.service;

import com.example.common_lib.dto.GetOrderDto;
import com.example.common_lib.dto.STATE_ORDER;
import com.example.controller.dto.CreateOrderDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    Page<GetOrderDto> getAll(Pageable pageable);

    GetOrderDto getOne(Long id);

    List<GetOrderDto> getMany(List<Long> ids);

    GetOrderDto create(CreateOrderDto dto);

    void changeOrderState(Long idOrder, STATE_ORDER status);
}
