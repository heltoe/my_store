package com.example.my_store.product.controller;

import com.example.my_store.product.controller.dto.CreateOrUdpateProductDto;
import com.example.my_store.product.controller.dto.GetProductDto;
import com.example.my_store.product.utils.ProductEntityFilter;
import com.example.my_store.product.service.ProductService;
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
@RequestMapping("/rest/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public PagedModel<GetProductDto> getAll(@ParameterObject @ModelAttribute ProductEntityFilter filter, @ParameterObject Pageable pageable) {
        Page<GetProductDto> productEntities = productService.getAll(filter, pageable);
        return new PagedModel<>(productEntities);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetProductDto> getOne(@PathVariable Long id) {
        var entity = productService.getOne(id);
        return ResponseEntity.status(HttpStatus.OK).body(entity);
    }

    @GetMapping("/by-ids")
    public ResponseEntity<List<GetProductDto>> getMany(@RequestParam List<Long> ids) {
        var list = productService.getMany(ids);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @PostMapping
    public ResponseEntity<GetProductDto> create(@RequestBody CreateOrUdpateProductDto dto) {
        var entity = productService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(entity);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GetProductDto> put(@PathVariable Long id, @RequestBody CreateOrUdpateProductDto dto) {
        var entity = productService.put(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(entity);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMany(@RequestParam List<Long> ids) {
        productService.deleteMany(ids);
        return ResponseEntity.noContent().build();
    }
}
