package com.example.my_store.courier.controller;

import com.example.my_store.courier.controller.dto.CreateUpdateCourierDto;
import com.example.my_store.courier.controller.dto.GetCourierDto;
import com.example.my_store.courier.service.CourierService;
import com.example.my_store.utils.openapi.StandardErrorResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/rest/couriers")
@RequiredArgsConstructor
@Tag(name = "Курьеры", description = "Управление курьерами")
public class CourierController {

    private final CourierService courierService;

    @Operation(summary = "Получить список курьеров")
    @ApiResponse(responseCode = "200", description = "Список курьеров успешно получен")
    @StandardErrorResponses
    @GetMapping
    public PagedModel<GetCourierDto> getAll(@ParameterObject Pageable pageable) {
        Page<GetCourierDto> getCourierDtos = courierService.getAll(pageable);
        return new PagedModel<>(getCourierDtos);
    }

    @Operation(summary = "Получить курьера по ID")
    @ApiResponse(responseCode = "200", description = "Курьер найден",
            content = @Content(schema = @Schema(implementation = GetCourierDto.class)))
    @StandardErrorResponses
    @GetMapping("/{id}")
    public ResponseEntity<GetCourierDto> getOne(
            @Parameter(description = "Идентификатор курьера", example = "1") @PathVariable Long id) {
        var result = courierService.getOne(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Operation(summary = "Получить курьеров по списку ID")
    @ApiResponse(responseCode = "200", description = "Курьеры найдены")
    @StandardErrorResponses
    @GetMapping("/by-ids")
    public ResponseEntity<List<GetCourierDto>> getMany(
            @Parameter(description = "Список идентификаторов курьеров", example = "1,2,3") @RequestParam List<Long> ids) {
        var result = courierService.getMany(ids);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Operation(summary = "Создать курьера")
    @ApiResponse(responseCode = "201", description = "Курьер создан",
            content = @Content(schema = @Schema(implementation = GetCourierDto.class)))
    @StandardErrorResponses
    @PostMapping
    public ResponseEntity<GetCourierDto> create(@RequestBody @Valid CreateUpdateCourierDto dto) {
        var result = courierService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Operation(summary = "Обновить курьера")
    @ApiResponse(responseCode = "200", description = "Курьер обновлён",
            content = @Content(schema = @Schema(implementation = GetCourierDto.class)))
    @StandardErrorResponses
    @PutMapping("/{id}")
    public ResponseEntity<GetCourierDto> patch(
            @Parameter(description = "Идентификатор курьера", example = "1") @PathVariable Long id,
            @RequestBody @Valid CreateUpdateCourierDto dto) {
        var result = courierService.patch(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Operation(summary = "Деактивировать курьера")
    @ApiResponse(responseCode = "204", description = "Курьер деактивирован")
    @StandardErrorResponses
    @PostMapping("/{id}/set-inactive")
    public ResponseEntity<Void> setInactiveCourier(
            @Parameter(description = "Идентификатор курьера", example = "1") @PathVariable Long id) {
        courierService.setInactiveCourier(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Активировать курьера")
    @ApiResponse(responseCode = "204", description = "Курьер активирован")
    @StandardErrorResponses
    @PostMapping("/{id}/set-active")
    public ResponseEntity<Void> setActiveCourier(
            @Parameter(description = "Идентификатор курьера", example = "1") @PathVariable Long id) {
        courierService.setActiveCourier(id);
        return ResponseEntity.noContent().build();
    }
}
