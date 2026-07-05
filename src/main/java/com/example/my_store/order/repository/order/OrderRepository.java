package com.example.my_store.order.repository.order;

import com.example.my_store.order.repository.order.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
}