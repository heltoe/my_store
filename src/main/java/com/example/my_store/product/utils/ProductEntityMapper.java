package com.example.my_store.product.utils;

import com.example.my_store.product.controller.dto.GetProductDto;
import com.example.my_store.product.controller.dto.CreateOrUdpateProductDto;
import com.example.my_store.product.repository.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductEntityMapper {
    ProductEntity convertToEntity(CreateOrUdpateProductDto createOrUdpateProductDto);

    ProductEntity convertToEntity(GetProductDto getProductDto);

    CreateOrUdpateProductDto convertToCreateOrUdpateProductDto(ProductEntity productEntity);

    GetProductDto convertToGetProductDto(ProductEntity productEntity);

    ProductEntity updateWithNull(CreateOrUdpateProductDto createOrUdpateProductDto, @MappingTarget ProductEntity productEntity);
}