package com.example.controller;

import com.example.common_lib.dto.GetProductDto;
import com.example.controller.dto.CreateOrUpdateProductDto;
import com.example.service.ProductService;
import com.example.utils.ProductEntityFilter;
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
@RequestMapping("/rest/products")
@RequiredArgsConstructor
@Tag(name = "Товары", description = "Управление товарами магазина")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Получить список товаров", description = "Возвращает постраничный список товаров с возможностью фильтрации")
    @ApiResponse(responseCode = "200", description = "Список товаров успешно получен")
    @StandardErrorResponses
    @GetMapping
    public PagedModel<GetProductDto> getAll(@ParameterObject @ModelAttribute ProductEntityFilter filter, @ParameterObject Pageable pageable) {
        Page<GetProductDto> productEntities = productService.getAll(filter, pageable);
        return new PagedModel<>(productEntities);
    }

    @Operation(summary = "Получить товар по ID")
    @ApiResponse(responseCode = "200", description = "Товар найден",
            content = @Content(schema = @Schema(implementation = GetProductDto.class)))
    @StandardErrorResponses
    @GetMapping("/{id}")
    public ResponseEntity<GetProductDto> getOne(
            @Parameter(description = "Идентификатор товара", example = "1") @PathVariable Long id) {
        var entity = productService.getOne(id);
        return ResponseEntity.status(HttpStatus.OK).body(entity);
    }

    @Operation(summary = "Получить товары по списку ID")
    @ApiResponse(responseCode = "200", description = "Товары найдены")
    @StandardErrorResponses
    @GetMapping("/by-ids")
    public ResponseEntity<List<GetProductDto>> getMany(
            @Parameter(description = "Список идентификаторов товаров", example = "1,2,3") @RequestParam List<Long> ids) {
        var list = productService.getMany(ids);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @Operation(summary = "Создать товар")
    @ApiResponse(responseCode = "201", description = "Товар создан",
            content = @Content(schema = @Schema(implementation = GetProductDto.class)))
    @StandardErrorResponses
    @PostMapping
    public ResponseEntity<GetProductDto> create(@RequestBody @Valid CreateOrUpdateProductDto dto) {
        var entity = productService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(entity);
    }

    @Operation(summary = "Обновить товар")
    @ApiResponse(responseCode = "200", description = "Товар обновлён",
            content = @Content(schema = @Schema(implementation = GetProductDto.class)))
    @StandardErrorResponses
    @PutMapping("/{id}")
    public ResponseEntity<GetProductDto> put(
            @Parameter(description = "Идентификатор товара", example = "1") @PathVariable Long id,
            @RequestBody @Valid CreateOrUpdateProductDto dto) {
        var entity = productService.put(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(entity);
    }

    @Operation(summary = "Деактивировать товар")
    @ApiResponse(responseCode = "204", description = "Товар деактивирован")
    @StandardErrorResponses
    @PostMapping("/{id}/set-inactive")
    public ResponseEntity<Void> setInactiveProduct(
            @Parameter(description = "Идентификатор товара", example = "1") @PathVariable Long id) {
        productService.setInactiveProduct(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Активировать товар")
    @ApiResponse(responseCode = "204", description = "Товар активирован")
    @StandardErrorResponses
    @PostMapping("/{id}/set-active")
    public ResponseEntity<Void> setActiveProduct(
            @Parameter(description = "Идентификатор товара", example = "1") @PathVariable Long id) {
        productService.setActiveProduct(id);
        return ResponseEntity.noContent().build();
    }
}
