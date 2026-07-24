package com.example.controller;

import com.example.common_lib.dto.GetDeliveryDto;
import com.example.controller.dto.CreateDeliveryDto;
import com.example.controller.dto.UpdateDeliveryDto;
import com.example.service.DeliveryService;
import com.example.utils.DeliveryEntityFilter;
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
@RequestMapping("/rest/deliveries")
@RequiredArgsConstructor
@Tag(name = "Доставки", description = "Управление доставками заказов")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @Operation(summary = "Получить список доставок", description = "Возвращает постраничный список доставок с возможностью фильтрации")
    @ApiResponse(responseCode = "200", description = "Список доставок успешно получен")
    @StandardErrorResponses
    @GetMapping
    public PagedModel<GetDeliveryDto> getAll(@ParameterObject @ModelAttribute DeliveryEntityFilter filter, @ParameterObject Pageable pageable) {
        Page<GetDeliveryDto> getDeliveryDtos = deliveryService.getAll(filter, pageable);
        return new PagedModel<>(getDeliveryDtos);
    }

    @Operation(summary = "Получить доставку по ID")
    @ApiResponse(responseCode = "200", description = "Доставка найдена",
            content = @Content(schema = @Schema(implementation = GetDeliveryDto.class)))
    @StandardErrorResponses
    @GetMapping("/{id}")
    public ResponseEntity<GetDeliveryDto> getOne(
            @Parameter(description = "Идентификатор доставки", example = "1") @PathVariable Long id) {
        var result =  deliveryService.getOne(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Operation(summary = "Получить доставки по списку ID")
    @ApiResponse(responseCode = "200", description = "Доставки найдены")
    @StandardErrorResponses
    @GetMapping("/by-ids")
    public ResponseEntity<List<GetDeliveryDto>> getMany(
            @Parameter(description = "Список идентификаторов доставок", example = "1,2,3") @RequestParam List<Long> ids) {
        var result =  deliveryService.getMany(ids);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Operation(summary = "Создать доставку")
    @ApiResponse(responseCode = "201", description = "Доставка создана",
            content = @Content(schema = @Schema(implementation = GetDeliveryDto.class)))
    @StandardErrorResponses
    @PostMapping
    public ResponseEntity<GetDeliveryDto> create(@RequestBody @Valid CreateDeliveryDto dto) {
        var result =  deliveryService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Operation(summary = "Обновить доставку")
    @ApiResponse(responseCode = "200", description = "Доставка обновлена",
            content = @Content(schema = @Schema(implementation = GetDeliveryDto.class)))
    @StandardErrorResponses
    @PatchMapping("/{id}")
    public ResponseEntity<GetDeliveryDto> patch(
            @Parameter(description = "Идентификатор доставки", example = "1") @PathVariable Long id,
            @RequestBody @Valid UpdateDeliveryDto dto) {
        var result = deliveryService.patch(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Operation(summary = "Удалить доставку")
    @ApiResponse(responseCode = "204", description = "Доставка удалена")
    @StandardErrorResponses
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Идентификатор доставки", example = "1") @PathVariable Long id) {
        deliveryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
