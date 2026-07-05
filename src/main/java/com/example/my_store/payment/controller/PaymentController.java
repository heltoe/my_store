package com.example.my_store.payment.controller;

import com.example.my_store.payment.controller.dto.CreateOrUpdatePaymentDto;
import com.example.my_store.payment.controller.dto.GetPaymentDto;
import com.example.my_store.payment.service.PaymentService;
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

    @GetMapping("/{id}")
    public ResponseEntity<GetPaymentDto> getOne(@PathVariable Long id) {
        var result = paymentService.getOne(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/by-ids")
    public ResponseEntity<List<GetPaymentDto>> getMany(@RequestParam List<Long> ids) {
        var result = paymentService.getMany(ids);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping
    public ResponseEntity<GetPaymentDto> create(@RequestBody CreateOrUpdatePaymentDto dto) {
        var result = paymentService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GetPaymentDto> patch(@PathVariable Long id) {
        var result = paymentService.patch(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
