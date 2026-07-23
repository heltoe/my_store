package com.example.my_store.order.utils;

import com.example.my_store.order.controller.dto.BaseCreateOrderDto;
import com.example.my_store.order.controller.dto.GetOrderDto;
import com.example.my_store.order.repository.order.entity.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderEntityMapper {
    OrderEntity convertToEntity(GetOrderDto getOrderDto);
    OrderEntity convertToEntity(BaseCreateOrderDto getOrderDto);
    GetOrderDto convertToGetOrderDto(OrderEntity orderEntity);
}