package com.example.my_store.delivery.controller;

import com.example.my_store.delivery.controller.dto.CreateDeliveryDto;
import com.example.my_store.delivery.controller.dto.GetDeliveryDto;
import com.example.my_store.delivery.controller.dto.UpdateDeliveryDto;
import com.example.my_store.delivery.service.DeliveryService;
import com.example.my_store.delivery.utils.DeliveryEntityFilter;
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
@RequestMapping("/rest/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping
    public PagedModel<GetDeliveryDto> getAll(@ParameterObject @ModelAttribute DeliveryEntityFilter filter, @ParameterObject Pageable pageable) {
        Page<GetDeliveryDto> getDeliveryDtos = deliveryService.getAll(filter, pageable);
        return new PagedModel<>(getDeliveryDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetDeliveryDto> getOne(@PathVariable Long id) {
        var result =  deliveryService.getOne(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/by-ids")
    public ResponseEntity<List<GetDeliveryDto>> getMany(@RequestParam List<Long> ids) {
        var result =  deliveryService.getMany(ids);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping
    public ResponseEntity<GetDeliveryDto> create(@RequestBody CreateDeliveryDto dto) {
        var result =  deliveryService.create(dto);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GetDeliveryDto> patch(@PathVariable Long id, @RequestBody UpdateDeliveryDto dto) {
        var result = deliveryService.patch(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deliveryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMany(@RequestParam List<Long> ids) {
        deliveryService.deleteMany(ids);
        return ResponseEntity.noContent().build();
    }
}
