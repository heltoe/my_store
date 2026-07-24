package com.example.utils;

import com.example.common_lib.dto.GetOrderDto;
import com.example.controller.dto.BaseCreateOrderDto;
import com.example.repository.order.entity.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderEntityMapper {
    OrderEntity convertToEntity(GetOrderDto getOrderDto);
    OrderEntity convertToEntity(BaseCreateOrderDto getOrderDto);
    GetOrderDto convertToGetOrderDto(OrderEntity orderEntity);
}