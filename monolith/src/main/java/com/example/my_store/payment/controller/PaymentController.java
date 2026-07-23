package com.example.my_store.payment.controller;

import com.example.my_store.payment.controller.dto.CreatePaymentDto;
import com.example.my_store.payment.controller.dto.GetPaymentDto;
import com.example.my_store.payment.service.PaymentService;
import com.example.my_store.utils.openapi.StandardErrorResponses;
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
@RequestMapping("/rest/payments")
@RequiredArgsConstructor
@Tag(name = "Платежи", description = "Управление платежами заказов")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Получить список платежей")
    @ApiResponse(responseCode = "200", description = "Список платежей успешно получен")
    @StandardErrorResponses
    @GetMapping
    public PagedModel<GetPaymentDto> getAll(@ParameterObject Pageable pageable) {
        Page<GetPaymentDto> getPaymentDtos = paymentService.getAll(pageable);
        return new PagedModel<>(getPaymentDtos);
    }

    @Operation(summary = "Получить платежи по списку ID")
    @ApiResponse(responseCode = "200", description = "Платежи найдены")
    @StandardErrorResponses
    @GetMapping("/by-ids")
    public ResponseEntity<List<GetPaymentDto>> getMany(
            @Parameter(description = "Список идентификаторов платежей", example = "1,2,3") @RequestParam List<Long> ids) {
        var result = paymentService.getMany(ids);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Operation(summary = "Получить платёж по ID заказа")
    @ApiResponse(responseCode = "200", description = "Платёж найден",
            content = @Content(schema = @Schema(implementation = GetPaymentDto.class)))
    @StandardErrorResponses
    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<GetPaymentDto> getByOrderId(
            @Parameter(description = "Идентификатор заказа", example = "1") @PathVariable Long orderId) {
        var result = paymentService.getByOrderId(orderId);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Operation(summary = "Получить платёж по ID")
    @ApiResponse(responseCode = "200", description = "Платёж найден",
            content = @Content(schema = @Schema(implementation = GetPaymentDto.class)))
    @StandardErrorResponses
    @GetMapping("/{id}")
    public ResponseEntity<GetPaymentDto> getOne(
            @Parameter(description = "Идентификатор платежа", example = "1") @PathVariable Long id) {
        var result = paymentService.getOne(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Operation(summary = "Создать платёж")
    @ApiResponse(responseCode = "201", description = "Платёж создан",
            content = @Content(schema = @Schema(implementation = GetPaymentDto.class)))
    @StandardErrorResponses
    @PostMapping
    public ResponseEntity<GetPaymentDto> create(@RequestBody @Valid CreatePaymentDto dto) {
        var result = paymentService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Operation(summary = "Отметить платёж как успешный")
    @ApiResponse(responseCode = "200", description = "Статус платежа обновлён",
            content = @Content(schema = @Schema(implementation = GetPaymentDto.class)))
    @StandardErrorResponses
    @PatchMapping("/{id}/mark-success")
    public ResponseEntity<GetPaymentDto> markSuccess(
            @Parameter(description = "Идентификатор платежа", example = "1") @PathVariable Long id) {
        var result = paymentService.markSuccess(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Operation(summary = "Отметить платёж как неуспешный")
    @ApiResponse(responseCode = "200", description = "Статус платежа обновлён",
            content = @Content(schema = @Schema(implementation = GetPaymentDto.class)))
    @StandardErrorResponses
    @PatchMapping("/{id}/mark-failure")
    public ResponseEntity<GetPaymentDto> markFailure(
            @Parameter(description = "Идентификатор платежа", example = "1") @PathVariable Long id) {
        var result = paymentService.markFailure(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
