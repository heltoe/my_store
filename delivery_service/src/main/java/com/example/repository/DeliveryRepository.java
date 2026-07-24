package com.example.repository;

import com.example.repository.entity.DeliveryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<DeliveryEntity, Long>, JpaSpecificationExecutor<DeliveryEntity> {
    Optional<DeliveryEntity> findByOrder_Id(Long id);
}