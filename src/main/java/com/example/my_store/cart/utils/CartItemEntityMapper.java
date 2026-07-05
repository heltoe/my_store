package com.example.my_store.cart.utils;

import com.example.my_store.cart.controller.dto.cart_item.CreateCartItemDto;
import com.example.my_store.cart.controller.dto.cart_item.GetCartItemDto;
import com.example.my_store.cart.controller.dto.cart_item.UpdateCartItemDto;
import com.example.my_store.cart.repository.cart_item.entity.CartItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CartItemEntityMapper {
    CartItemEntity convertToEntity(GetCartItemDto dto);

    CartItemEntity convertToEntity(CreateCartItemDto dto);

    CartItemEntity convertToEntity(UpdateCartItemDto dto);

    GetCartItemDto convertToGetCartItemDto(CartItemEntity entity);

    CartItemEntity updateWithNull(UpdateCartItemDto dto, @MappingTarget CartItemEntity entity);
}