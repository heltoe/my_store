package com.example.service;

import com.example.common_lib.dto.GetCourierDto;
import com.example.controller.dto.CreateUpdateCourierDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CourierService {
    Page<GetCourierDto> getAll(Pageable pageable);

    GetCourierDto getOne(Long id);

    List<GetCourierDto> getMany(List<Long> ids);

    GetCourierDto create(CreateUpdateCourierDto dto);

    GetCourierDto patch(Long id, CreateUpdateCourierDto dto);

    void setInactiveCourier(Long id);

    void setActiveCourier(Long id);
}
