package com.example.service;

import com.example.common_lib.dto.GetAccountDto;
import com.example.controller.dto.CreateOrUpdateAccountDto;
import com.example.utils.AccountEntityFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AccountService {
    Page<GetAccountDto> getAll(AccountEntityFilter filter, Pageable pageable);

    GetAccountDto getOne(Long id);

    List<GetAccountDto> getMany(List<Long> ids);

    GetAccountDto create(CreateOrUpdateAccountDto dto);

    GetAccountDto put(Long id, CreateOrUpdateAccountDto dto);

    void delete(Long id);

    void deleteMany(List<Long> ids);
}
