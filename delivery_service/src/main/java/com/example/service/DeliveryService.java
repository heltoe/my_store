package com.example.service;

import com.example.common_lib.dto.GetDeliveryDto;
import com.example.controller.dto.CreateDeliveryDto;
import com.example.controller.dto.UpdateDeliveryDto;
import com.example.utils.DeliveryEntityFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DeliveryService {
    Page<GetDeliveryDto> getAll(DeliveryEntityFilter filter, Pageable pageable);

    GetDeliveryDto getOne(Long id);

    List<GetDeliveryDto> getMany(List<Long> ids);

    GetDeliveryDto create(CreateDeliveryDto dto);

    GetDeliveryDto patch(Long id, UpdateDeliveryDto dto);

    void delete(Long id);
}
