package com.example.my_store.account.service;

import com.example.my_store.account.utils.AccountEntityFilter;
import com.example.my_store.account.controller.dto.CreateOrUpdateAccountDto;
import com.example.my_store.account.controller.dto.GetAccountDto;
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
