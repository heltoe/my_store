package com.example.my_store.payment.controller;

import com.example.my_store.payment.controller.dto.CreatePaymentDto;
import com.example.my_store.payment.controller.dto.GetPaymentDto;
import com.example.my_store.payment.service.PaymentService;
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
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public PagedModel<GetPaymentDto> getAll(@ParameterObject Pageable pageable) {
        Page<GetPaymentDto> getPaymentDtos = paymentService.getAll(pageable);
        return new PagedModel<>(getPaymentDtos);
    }

    @GetMapping("/by-ids")
    public ResponseEntity<List<GetPaymentDto>> getMany(@RequestParam List<Long> ids) {
        var result = paymentService.getMany(ids);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<GetPaymentDto> getByOrderId(@PathVariable Long orderId) {
        var result = paymentService.getByOrderId(orderId);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetPaymentDto> getOne(@PathVariable Long id) {
        var result = paymentService.getOne(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping
    public ResponseEntity<GetPaymentDto> create(@RequestBody @Valid CreatePaymentDto dto) {
        var result = paymentService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PatchMapping("/{id}/mark-success")
    public ResponseEntity<GetPaymentDto> markSuccess(@PathVariable Long id) {
        var result = paymentService.markSuccess(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PatchMapping("/{id}/mark-failure")
    public ResponseEntity<GetPaymentDto> markFailure(@PathVariable Long id) {
        var result = paymentService.markFailure(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
