package com.example.my_store.product.utils;

import com.example.my_store.product.controller.dto.GetProductDto;
import com.example.my_store.product.controller.dto.CreateOrUpdateProductDto;
import com.example.my_store.product.repository.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductEntityMapper {
    ProductEntity convertToEntity(CreateOrUpdateProductDto createOrUpdateProductDto);

    ProductEntity convertToEntity(GetProductDto getProductDto);

    CreateOrUpdateProductDto convertToCreateOrUpdateProductDto(ProductEntity productEntity);

    GetProductDto convertToGetProductDto(ProductEntity productEntity);

    ProductEntity updateWithNull(CreateOrUpdateProductDto createOrUpdateProductDto, @MappingTarget ProductEntity productEntity);
}