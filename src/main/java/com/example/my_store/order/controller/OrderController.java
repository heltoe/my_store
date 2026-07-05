package com.example.my_store.order.controller;

import com.example.my_store.order.controller.dto.CreateOrderDto;
import com.example.my_store.order.controller.dto.GetOrderDto;
import com.example.my_store.order.service.OrderService;
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
@RequestMapping("/rest/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public PagedModel<GetOrderDto> getAll(@ParameterObject Pageable pageable) {
        Page<GetOrderDto> getOrderDtos = orderService.getAll(pageable);
        return new PagedModel<>(getOrderDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetOrderDto> getOne(@PathVariable Long id) {
        var entity = orderService.getOne(id);
        return ResponseEntity.status(HttpStatus.OK).body(entity);
    }

    @GetMapping("/by-ids")
    public ResponseEntity<List<GetOrderDto>> getMany(@RequestParam List<Long> ids) {
        var list = orderService.getMany(ids);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @PostMapping
    public ResponseEntity<GetOrderDto> create(@RequestBody CreateOrderDto dto) {
        var entity =  orderService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(entity);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMany(@RequestParam List<Long> ids) {
        orderService.deleteMany(ids);
        return ResponseEntity.noContent().build();
    }
}
