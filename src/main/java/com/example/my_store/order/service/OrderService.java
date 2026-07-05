package com.example.my_store.order.service;

import com.example.my_store.order.controller.dto.ChangeOrderStatusDto;
import com.example.my_store.order.controller.dto.CreateOrderDto;
import com.example.my_store.order.controller.dto.GetOrderDto;
import com.example.my_store.order.repository.order.entity.STATE_ORDER;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

public interface OrderService {
    Page<GetOrderDto> getAll(Pageable pageable);

    GetOrderDto getOne(Long id);

    List<GetOrderDto> getMany(List<Long> ids);

    GetOrderDto create(CreateOrderDto dto);

    void changeOrderState(Long idOrder, STATE_ORDER status);

    void delete(Long id);

    void deleteMany(List<Long> ids);
}
