package com.example.my_store.cart.controller;

import com.example.my_store.cart.controller.dto.cart.GetCartDto;
import com.example.my_store.cart.controller.dto.cart_item.CreateCartItemDto;
import com.example.my_store.cart.controller.dto.cart_item.UpdateCartItemDto;
import com.example.my_store.cart.controller.dto.cart.CreateCartDto;
import com.example.my_store.cart.service.CartService;
import com.example.my_store.utils.openapi.StandardErrorResponses;
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
@RequestMapping("/rest/carts")
@RequiredArgsConstructor
@Tag(name = "Корзины", description = "Управление корзинами покупателей")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Получить список корзин")
    @ApiResponse(responseCode = "200", description = "Список корзин успешно получен")
    @StandardErrorResponses
    @GetMapping
    public PagedModel<GetCartDto> getAll(@ParameterObject Pageable pageable) {
        Page<GetCartDto> getCartDtos = cartService.getAll(pageable);
        return new PagedModel<>(getCartDtos);
    }

    @Operation(summary = "Получить корзину по ID")
    @ApiResponse(responseCode = "200", description = "Корзина найдена",
            content = @Content(schema = @Schema(implementation = GetCartDto.class)))
    @StandardErrorResponses
    @GetMapping("/{id}")
    public ResponseEntity<GetCartDto> getOne(
            @Parameter(description = "Идентификатор корзины", example = "1") @PathVariable Long id) {
        var entity = cartService.getOne(id);
        return ResponseEntity.status(HttpStatus.OK).body(entity);
    }

    @Operation(summary = "Получить корзины по списку ID")
    @ApiResponse(responseCode = "200", description = "Корзины найдены")
    @StandardErrorResponses
    @GetMapping("/by-ids")
    public ResponseEntity<List<GetCartDto>> getMany(
            @Parameter(description = "Список идентификаторов корзин", example = "1,2,3") @RequestParam List<Long> ids) {
        var list = cartService.getMany(ids);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @Operation(summary = "Создать корзину")
    @ApiResponse(responseCode = "201", description = "Корзина создана",
            content = @Content(schema = @Schema(implementation = GetCartDto.class)))
    @StandardErrorResponses
    @PostMapping
    public ResponseEntity<GetCartDto> create(@RequestBody @Valid CreateCartDto dto) {
        var entity = cartService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(entity);
    }

    @Operation(summary = "Добавить товар в корзину")
    @ApiResponse(responseCode = "204", description = "Товар добавлен в корзину")
    @StandardErrorResponses
    @PostMapping("/add")
    public ResponseEntity<GetCartDto> addToCart(@RequestBody @Valid CreateCartItemDto dto) {
        cartService.addToCart(dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Изменить количество товара в корзине")
    @ApiResponse(responseCode = "204", description = "Количество товара обновлено")
    @StandardErrorResponses
    @PostMapping("/change-quantity")
    public ResponseEntity<GetCartDto> changeQuantity(@RequestBody @Valid UpdateCartItemDto dto) {
        cartService.changeQuantity(dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить позицию из корзины")
    @ApiResponse(responseCode = "204", description = "Позиция удалена из корзины")
    @StandardErrorResponses
    @DeleteMapping("/remove/{id}")
    public ResponseEntity<Void> removeFromCart(
            @Parameter(description = "Идентификатор позиции корзины", example = "1") @PathVariable Long id) {
        cartService.removeFromCart(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить несколько позиций из корзины")
    @ApiResponse(responseCode = "204", description = "Позиции удалены из корзины")
    @StandardErrorResponses
    @DeleteMapping
    public ResponseEntity<Void> removeManyFromCart(
            @Parameter(description = "Список идентификаторов позиций корзины", example = "1,2,3") @RequestParam List<Long> ids) {
        cartService.removeManyFromCart(ids);
        return ResponseEntity.noContent().build();
    }
}
