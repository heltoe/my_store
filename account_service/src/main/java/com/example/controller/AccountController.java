package com.example.controller;

import com.example.common_lib.dto.GetAccountDto;
import com.example.controller.dto.CreateOrUpdateAccountDto;
import com.example.service.AccountService;
import com.example.utils.AccountEntityFilter;
import com.example.common_lib.utils.openapi.StandardErrorResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Аккаунты", description = "Управление аккаунтами покупателей")
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Получить список аккаунтов", description = "Возвращает постраничный список аккаунтов с возможностью фильтрации")
    @ApiResponse(responseCode = "200", description = "Список аккаунтов успешно получен")
    @StandardErrorResponses
    @GetMapping
    public PagedModel<GetAccountDto> getAll(@ParameterObject @ModelAttribute AccountEntityFilter filter, @ParameterObject Pageable pageable) {
        Page<GetAccountDto> accounts = accountService.getAll(filter, pageable);
        return new PagedModel<>(accounts);
    }

    @Operation(summary = "Получить аккаунт по ID")
    @ApiResponse(responseCode = "200", description = "Аккаунт найден",
            content = @Content(schema = @Schema(implementation = GetAccountDto.class)))
    @StandardErrorResponses
    @GetMapping("/{id}")
    public ResponseEntity<GetAccountDto> getOne(
            @Parameter(description = "Идентификатор аккаунта", example = "1") @PathVariable Long id) {
        var entity = accountService.getOne(id);
        return ResponseEntity.status(HttpStatus.OK).body(entity);
    }

    @Operation(summary = "Получить аккаунты по списку ID")
    @ApiResponse(responseCode = "200", description = "Аккаунты найдены")
    @StandardErrorResponses
    @GetMapping("/by-ids")
    public ResponseEntity<List<GetAccountDto>> getMany(
            @Parameter(description = "Список идентификаторов аккаунтов", example = "1,2,3") @RequestParam List<Long> ids) {
        var list = accountService.getMany(ids);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @Operation(summary = "Создать аккаунт")
    @ApiResponse(responseCode = "201", description = "Аккаунт создан",
            content = @Content(schema = @Schema(implementation = GetAccountDto.class)))
    @StandardErrorResponses
    @PostMapping
    public ResponseEntity<GetAccountDto> create(@RequestBody @Valid CreateOrUpdateAccountDto dto) {
        var entity = accountService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(entity);
    }

    @Operation(summary = "Обновить аккаунт")
    @ApiResponse(responseCode = "200", description = "Аккаунт обновлён",
            content = @Content(schema = @Schema(implementation = GetAccountDto.class)))
    @StandardErrorResponses
    @PutMapping("/{id}")
    public ResponseEntity<GetAccountDto> patch(
            @Parameter(description = "Идентификатор аккаунта", example = "1") @PathVariable Long id,
            @RequestBody @Valid CreateOrUpdateAccountDto dto) {
        var entity = accountService.put(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(entity);
    }

    @Operation(summary = "Удалить аккаунт")
    @ApiResponse(responseCode = "204", description = "Аккаунт удалён")
    @StandardErrorResponses
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Идентификатор аккаунта", example = "1") @PathVariable Long id) {
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить несколько аккаунтов")
    @ApiResponse(responseCode = "204", description = "Аккаунты удалены")
    @StandardErrorResponses
    @DeleteMapping
    public ResponseEntity<Void> deleteMany(
            @Parameter(description = "Список идентификаторов аккаунтов", example = "1,2,3") @RequestParam List<Long> ids) {
        accountService.deleteMany(ids);
        return ResponseEntity.noContent().build();
    }
}
