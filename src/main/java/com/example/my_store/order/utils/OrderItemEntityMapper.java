package com.example.my_store.order.utils;

import com.example.my_store.order.controller.dto.CreateOrderItemDto;
import com.example.my_store.order.repository.order_item.entity.OrderItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderItemEntityMapper {
    OrderItemEntity convertToEntity(CreateOrderItemDto dto);
}
