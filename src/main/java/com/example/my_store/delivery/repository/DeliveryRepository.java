package com.example.my_store.delivery.repository;

import com.example.my_store.delivery.repository.entity.DeliveryEntity;
import com.example.my_store.order.repository.order.entity.STATE_ORDER;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<DeliveryEntity, Long>, JpaSpecificationExecutor<DeliveryEntity> {
    Optional<DeliveryEntity> findByOrder_Id(Long id);

    boolean existsByCourier_IdAndOrder_Status(Long courierId, STATE_ORDER status);
}