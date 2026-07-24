package com.example.utils;

import com.example.common_lib.dto.GetAccountDto;
import com.example.controller.dto.CreateOrUpdateAccountDto;
import com.example.repository.entity.AccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountEntityMapper {
    AccountEntity convertToEntity(CreateOrUpdateAccountDto createOrUpdateAccountDto);

    AccountEntity convertToEntity(GetAccountDto getAccountDto);

    CreateOrUpdateAccountDto convertToCreateAccountDto(AccountEntity accountEntity);

    GetAccountDto convertToGetAccountDto(AccountEntity accountEntity);

    AccountEntity updateWithNull(CreateOrUpdateAccountDto createOrUpdateAccountDto, @MappingTarget AccountEntity accountEntity);
}