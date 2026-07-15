package com.example.my_store.order.repository.order_item;

import com.example.my_store.order.repository.order.entity.STATE_ORDER;
import com.example.my_store.order.repository.order_item.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
    boolean existsByProduct_IdAndOrder_StatusNotIn(Long productId, Collection<STATE_ORDER> statuses);
}
