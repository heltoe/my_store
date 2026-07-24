package com.example.service;

import com.example.common_lib.dto.GetAccountDto;
import com.example.controller.dto.CreateOrUpdateAccountDto;
import com.example.repository.AccountRepository;
import com.example.repository.entity.AccountEntity;
import com.example.utils.AccountEntityFilter;
import com.example.utils.AccountEntityMapper;
import com.example.common_lib.utils.exception.CommonConflictException;
import com.example.common_lib.utils.exception.CommonEntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountEntityMapper accountEntityMapper;

    private final AccountRepository accountRepository;

    private AccountEntity getRequiredAccount(Long id) {
        return accountRepository.findById(id).orElseThrow(() ->
                new CommonEntityNotFoundException("Entity with id `%s` not found".formatted(id)));
    }

    private void throwIfPhoneNumberExists(String phoneNumber) {
        if (accountRepository.existsByPhoneNumber(phoneNumber)) {
            throw new CommonConflictException("Entity with phone number `%s` already exists".formatted(phoneNumber));
        }
    }

    private void throwIfPhoneNumberExistsForAnotherAccount(String phoneNumber, Long id) {
        if (accountRepository.existsByPhoneNumberAndIdNot(phoneNumber, id)) {
            throw new CommonConflictException("Entity with phone number `%s` already exists".formatted(phoneNumber));
        }
    }

    @Override
    public Page<GetAccountDto> getAll(AccountEntityFilter filter, Pageable pageable) {
        Specification<AccountEntity> spec = filter.toSpecification();
        Page<AccountEntity> accountEntities = accountRepository.findAll(spec, pageable);
        return accountEntities.map(accountEntityMapper::convertToGetAccountDto);
    }

    @Override
    public GetAccountDto getOne(Long id) {
        AccountEntity accountEntity = getRequiredAccount(id);
        return accountEntityMapper.convertToGetAccountDto(accountEntity);
    }

    @Override
    public List<GetAccountDto> getMany(List<Long> ids) {
        List<AccountEntity> accountEntities = accountRepository.findAllById(ids);
        return accountEntities.stream()
                .map(accountEntityMapper::convertToGetAccountDto)
                .toList();
    }

    @Override
    public GetAccountDto create(CreateOrUpdateAccountDto dto) {
        throwIfPhoneNumberExists(dto.phoneNumber());
        AccountEntity accountEntity = accountEntityMapper.convertToEntity(dto);
        AccountEntity resultAccountEntity = accountRepository.save(accountEntity);
        return accountEntityMapper.convertToGetAccountDto(resultAccountEntity);
    }

    @Override
    public GetAccountDto put(Long id, CreateOrUpdateAccountDto dto) {
        AccountEntity accountEntity = getRequiredAccount(id);

        throwIfPhoneNumberExistsForAnotherAccount(dto.phoneNumber(), id);
        accountEntityMapper.updateWithNull(dto, accountEntity);

        AccountEntity resultAccountEntity = accountRepository.save(accountEntity);
        return accountEntityMapper.convertToGetAccountDto(resultAccountEntity);
    }

    @Override
    public void delete(Long id) {
        AccountEntity accountEntity = getRequiredAccount(id);
        accountRepository.delete(accountEntity);
    }

    @Override
    public void deleteMany(List<Long> ids) {
        accountRepository.deleteAllById(ids);
    }
}
