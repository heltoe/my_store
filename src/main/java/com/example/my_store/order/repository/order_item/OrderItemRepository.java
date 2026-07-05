package com.example.my_store.order.repository.order_item;

import com.example.my_store.order.repository.order_item.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
}
