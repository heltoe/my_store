package com.example.my_store.courier.service;

import com.example.my_store.courier.controller.dto.CreateUpdateCourierDto;
import com.example.my_store.courier.controller.dto.GetCourierDto;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

public interface CourierService {
    Page<GetCourierDto> getAll(Pageable pageable);

    GetCourierDto getOne(Long id);

    List<GetCourierDto> getMany(List<Long> ids);

    GetCourierDto create(CreateUpdateCourierDto dto);

    GetCourierDto patch(Long id, CreateUpdateCourierDto dto);

    void delete(Long id);

    void deleteMany(List<Long> ids);
}
