package com.example.utils;

import com.example.controller.dto.cart.CreateCartDto;
import com.example.controller.dto.cart.GetCartDto;
import com.example.repository.cart.entity.CartEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CartEntityMapper {
    CartEntity convertToEntity(CreateCartDto createCartDto);

    GetCartDto convertToGetCartDto(CartEntity cartEntity);
}