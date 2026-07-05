package com.example.my_store.account.service;

import com.example.my_store.account.utils.AccountEntityFilter;
import com.example.my_store.account.utils.AccountEntityMapper;
import com.example.my_store.account.controller.dto.CreateOrUpdateAccountDto;
import com.example.my_store.account.controller.dto.GetAccountDto;
import com.example.my_store.account.repository.entity.AccountEntity;
import com.example.my_store.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountEntityMapper accountEntityMapper;

    private final AccountRepository accountRepository;

    public AccountEntity _getOne(Long id) {
        return accountRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));
    }

    @Override
    public Page<GetAccountDto> getAll(AccountEntityFilter filter, Pageable pageable) {
        Specification<AccountEntity> spec = filter.toSpecification();
        Page<AccountEntity> accountEntities = accountRepository.findAll(spec, pageable);
        return accountEntities.map(accountEntityMapper::convertToGetAccountDto);
    }

    @Override
    public GetAccountDto getOne(Long id) {
        AccountEntity accountEntity = _getOne(id);
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
        AccountEntity accountEntity = accountEntityMapper.convertToEntity(dto);
        AccountEntity resultAccountEntity = accountRepository.save(accountEntity);
        return accountEntityMapper.convertToGetAccountDto(resultAccountEntity);
    }

    @Override
    public GetAccountDto put(Long id, CreateOrUpdateAccountDto dto) {
        AccountEntity accountEntity = _getOne(id);

        accountEntityMapper.updateWithNull(dto, accountEntity);

        AccountEntity resultAccountEntity = accountRepository.save(accountEntity);
        return accountEntityMapper.convertToGetAccountDto(resultAccountEntity);
    }

    @Override
    public void delete(Long id) {
        AccountEntity accountEntity = _getOne(id);
        if (accountEntity != null) {
            accountRepository.delete(accountEntity);
        }
    }

    @Override
    public void deleteMany(List<Long> ids) {
        accountRepository.deleteAllById(ids);
    }
}
