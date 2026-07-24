package com.example.utils;

import com.example.controller.dto.CreatePaymentDto;
import com.example.controller.dto.GetPaymentDto;
import com.example.repository.entity.PaymentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentEntityMapper {
    GetPaymentDto convertToGetPaymentDto(PaymentEntity paymentEntity);

    PaymentEntity convertToEntity(CreatePaymentDto createPaymentDto);
}
