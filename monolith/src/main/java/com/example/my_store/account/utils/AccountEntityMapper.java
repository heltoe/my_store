package com.example.my_store.account.utils;

import com.example.my_store.account.controller.dto.CreateOrUpdateAccountDto;
import com.example.my_store.account.controller.dto.GetAccountDto;
import com.example.my_store.account.repository.entity.AccountEntity;
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