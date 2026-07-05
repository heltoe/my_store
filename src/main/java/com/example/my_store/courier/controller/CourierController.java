package com.example.my_store.courier.controller;

import com.example.my_store.courier.controller.dto.CreateUpdateCourierDto;
import com.example.my_store.courier.controller.dto.GetCourierDto;
import com.example.my_store.courier.service.CourierService;
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
@RequestMapping("/rest/couriers")
@RequiredArgsConstructor
public class CourierController {

    private final CourierService courierService;

    @GetMapping
    public PagedModel<GetCourierDto> getAll(@ParameterObject Pageable pageable) {
        Page<GetCourierDto> getCourierDtos = courierService.getAll(pageable);
        return new PagedModel<>(getCourierDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetCourierDto> getOne(@PathVariable Long id) {
        var result = courierService.getOne(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/by-ids")
    public ResponseEntity<List<GetCourierDto>> getMany(@RequestParam List<Long> ids) {
        var result = courierService.getMany(ids);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping
    public ResponseEntity<GetCourierDto> create(@RequestBody CreateUpdateCourierDto dto) {
        var result = courierService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GetCourierDto> patch(@PathVariable Long id, @RequestBody CreateUpdateCourierDto dto) {
        var result = courierService.patch(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courierService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMany(@RequestParam List<Long> ids) {
        courierService.deleteMany(ids);
        return ResponseEntity.noContent().build();
    }
}
