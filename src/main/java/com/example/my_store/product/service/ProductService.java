package com.example.my_store.product.service;

import com.example.my_store.product.controller.dto.CreateOrUdpateProductDto;
import com.example.my_store.product.controller.dto.GetProductDto;
import com.example.my_store.product.utils.ProductEntityFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    Page<GetProductDto> getAll(ProductEntityFilter filter, Pageable pageable);

    GetProductDto getOne(Long id);

    List<GetProductDto> getMany(List<Long> ids);

    GetProductDto create(CreateOrUdpateProductDto productEntity);

    GetProductDto put(Long id, CreateOrUdpateProductDto dto);

    void delete(Long id);

    void deleteMany(List<Long> ids);
}
