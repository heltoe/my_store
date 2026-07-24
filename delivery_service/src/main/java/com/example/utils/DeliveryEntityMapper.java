package com.example.utils;

import com.example.common_lib.dto.GetDeliveryDto;
import com.example.controller.dto.CreateDeliveryDto;
import com.example.controller.dto.UpdateDeliveryDto;
import com.example.repository.entity.DeliveryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface DeliveryEntityMapper {
    DeliveryEntity convertToEntity(GetDeliveryDto getDeliveryDto);

    GetDeliveryDto convertToGetDeliveryDto(DeliveryEntity deliveryEntity);

    DeliveryEntity updateWithNull(UpdateDeliveryDto updateDeliveryDto, @MappingTarget DeliveryEntity deliveryEntity);

    DeliveryEntity convertToEntity(CreateDeliveryDto createDeliveryDto);
}