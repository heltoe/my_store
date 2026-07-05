package com.example.my_store.account.controller;

import com.example.my_store.account.utils.AccountEntityFilter;
import com.example.my_store.account.controller.dto.CreateOrUpdateAccountDto;
import com.example.my_store.account.controller.dto.GetAccountDto;
import com.example.my_store.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public PagedModel<GetAccountDto> getAll(@ParameterObject @ModelAttribute AccountEntityFilter filter, @ParameterObject Pageable pageable) {
        Page<GetAccountDto> accounts = accountService.getAll(filter, pageable);
        return new PagedModel<>(accounts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetAccountDto> getOne(@PathVariable Long id) {
        var entity = accountService.getOne(id);
        return ResponseEntity.status(HttpStatus.OK).body(entity);
    }

    @GetMapping("/by-ids")
    public ResponseEntity<List<GetAccountDto>> getMany(@RequestParam List<Long> ids) {
        var list = accountService.getMany(ids);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @PostMapping
    public ResponseEntity<GetAccountDto> create(@RequestBody @Valid CreateOrUpdateAccountDto dto) {
        var entity = accountService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(entity);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GetAccountDto> patch(@PathVariable Long id, @RequestBody @Valid CreateOrUpdateAccountDto dto) {
        var entity = accountService.put(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(entity);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMany(@RequestParam List<Long> ids) {
        accountService.deleteMany(ids);
        return ResponseEntity.noContent().build();
    }
}
