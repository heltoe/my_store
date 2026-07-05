package com.example.my_store.cart.repository.cart_item;

import com.example.my_store.cart.repository.cart_item.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
}