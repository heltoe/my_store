package com.example.my_store.cart.controller;

import com.example.my_store.cart.controller.dto.cart.GetCartDto;
import com.example.my_store.cart.controller.dto.cart_item.CreateCartItemDto;
import com.example.my_store.cart.controller.dto.cart_item.UpdateCartItemDto;
import com.example.my_store.cart.service.CartService;
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
public class CartController {

    private final CartService cartService;

    @GetMapping
    public PagedModel<GetCartDto> getAll(@ParameterObject Pageable pageable) {
        Page<GetCartDto> getCartDtos = cartService.getAll(pageable);
        return new PagedModel<>(getCartDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetCartDto> getOne(@PathVariable Long id) {
        var entity = cartService.getOne(id);
        return ResponseEntity.status(HttpStatus.OK).body(entity);
    }

    @GetMapping("/by-ids")
    public ResponseEntity<List<GetCartDto>> getMany(@RequestParam List<Long> ids) {
        var list = cartService.getMany(ids);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @PostMapping
    public ResponseEntity<GetCartDto> create(@RequestBody GetCartDto dto) {
        var entity = cartService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(entity);
    }

    @PostMapping("/add")
    public ResponseEntity<GetCartDto> addToCart(@RequestBody CreateCartItemDto dto) {
        cartService.addToCart(dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-quantity")
    public ResponseEntity<GetCartDto> changeQuantity(@RequestBody UpdateCartItemDto dto) {
        cartService.changeQuantity(dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long id) {
        cartService.removeFromCart(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> removeManyFromCart(@RequestParam List<Long> ids) {
        cartService.removeManyFromCart(ids);
        return ResponseEntity.noContent().build();
    }
}
