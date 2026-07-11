package com.example.my_store.cart.service;

import com.example.my_store.cart.controller.dto.cart.GetCartDto;
import com.example.my_store.cart.controller.dto.cart.CreateCartDto;
import com.example.my_store.cart.controller.dto.cart_item.CreateCartItemDto;
import com.example.my_store.cart.controller.dto.cart_item.UpdateCartItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CartService {
    Page<GetCartDto> getAll(Pageable pageable);

    GetCartDto getOne(Long id);

    List<GetCartDto> getMany(List<Long> ids);

    GetCartDto create(CreateCartDto dto);

    void addToCart(CreateCartItemDto dto);

    void changeQuantity(UpdateCartItemDto dto);

    void removeFromCart(Long id);

    void removeManyFromCart(List<Long> ids);
}
