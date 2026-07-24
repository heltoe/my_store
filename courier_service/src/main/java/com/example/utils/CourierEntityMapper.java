package com.example.utils;

import com.example.common_lib.dto.GetCourierDto;
import com.example.controller.dto.CreateUpdateCourierDto;
import com.example.repository.entity.CourierEntity;
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