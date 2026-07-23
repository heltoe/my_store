package com.example.my_store.order.controller;

import com.example.my_store.order.controller.dto.CreateOrderDto;
import com.example.my_store.order.controller.dto.GetOrderDto;
import com.example.my_store.order.service.OrderService;
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
import com.example.my_store.order.controller.dto.ChangeOrderStatusDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/rest/orders")
@RequiredArgsConstructor
@Tag(name = "Заказы", description = "Управление заказами")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Получить список заказов")
    @ApiResponse(responseCode = "200", description = "Список заказов успешно получен")
    @StandardErrorResponses
    @GetMapping
    public PagedModel<GetOrderDto> getAll(@ParameterObject Pageable pageable) {
        Page<GetOrderDto> getOrderDtos = orderService.getAll(pageable);
        return new PagedModel<>(getOrderDtos);
    }

    @Operation(summary = "Получить заказ по ID")
    @ApiResponse(responseCode = "200", description = "Заказ найден",
            content = @Content(schema = @Schema(implementation = GetOrderDto.class)))
    @StandardErrorResponses
    @GetMapping("/{id}")
    public ResponseEntity<GetOrderDto> getOne(
            @Parameter(description = "Идентификатор заказа", example = "1") @PathVariable Long id) {
        var entity = orderService.getOne(id);
        return ResponseEntity.status(HttpStatus.OK).body(entity);
    }

    @Operation(summary = "Получить заказы по списку ID")
    @ApiResponse(responseCode = "200", description = "Заказы найдены")
    @StandardErrorResponses
    @GetMapping("/by-ids")
    public ResponseEntity<List<GetOrderDto>> getMany(
            @Parameter(description = "Список идентификаторов заказов", example = "1,2,3") @RequestParam List<Long> ids) {
        var list = orderService.getMany(ids);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @Operation(summary = "Создать заказ")
    @ApiResponse(responseCode = "201", description = "Заказ создан",
            content = @Content(schema = @Schema(implementation = GetOrderDto.class)))
    @StandardErrorResponses
    @PostMapping
    public ResponseEntity<GetOrderDto> create(@RequestBody @Valid CreateOrderDto dto) {
        var entity =  orderService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(entity);
    }

    @Operation(summary = "Изменить статус заказа")
    @ApiResponse(responseCode = "204", description = "Статус заказа обновлён")
    @StandardErrorResponses
    @PatchMapping("/{id}/change-status")
    public ResponseEntity<Void> changeOrderState(
            @Parameter(description = "Идентификатор заказа", example = "1") @PathVariable Long id,
            @RequestBody @Valid ChangeOrderStatusDto dto) {
        orderService.changeOrderState(id, dto.status());
        return ResponseEntity.noContent().build();
    }
}
