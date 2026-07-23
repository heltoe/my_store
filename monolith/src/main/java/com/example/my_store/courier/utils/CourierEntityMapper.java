package com.example.my_store.courier.utils;

import com.example.my_store.courier.controller.dto.CreateUpdateCourierDto;
import com.example.my_store.courier.controller.dto.GetCourierDto;
import com.example.my_store.courier.repository.entity.CourierEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CourierEntityMapper {
    CourierEntity convertToEntity(GetCourierDto getCourierDto);

    CourierEntity convertToEntity(CreateUpdateCourierDto createUpdateCourierDto);

    GetCourierDto convertToGetCourierDto(CourierEntity courierEntity);

    CourierEntity updateWithNull(CreateUpdateCourierDto getCourierDto, @MappingTarget CourierEntity courierEntity);
}