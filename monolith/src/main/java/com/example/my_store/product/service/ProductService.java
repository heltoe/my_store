package com.example.my_store.product.service;

import com.example.my_store.product.controller.dto.CreateOrUpdateProductDto;
import com.example.my_store.product.controller.dto.GetProductDto;
import com.example.my_store.product.repository.entity.ProductEntity;
import com.example.my_store.product.utils.ProductEntityFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    Page<GetProductDto> getAll(ProductEntityFilter filter, Pageable pageable);

    GetProductDto getOne(Long id);

    List<GetProductDto> getMany(List<Long> ids);

    GetProductDto create(CreateOrUpdateProductDto productEntity);

    GetProductDto put(Long id, CreateOrUpdateProductDto dto);

    void setActiveProduct(Long id);

    void setInactiveProduct(Long id);

    void requireActiveProduct(ProductEntity productEntity);

    ProductEntity getRequiredActiveProduct(Long id);
}
