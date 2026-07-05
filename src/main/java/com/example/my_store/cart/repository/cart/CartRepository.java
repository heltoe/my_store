package com.example.my_store.cart.repository.cart;

import com.example.my_store.cart.repository.cart.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<CartEntity, Long> {
}