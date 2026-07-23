package com.example.my_store.payment.repository;

import com.example.my_store.payment.repository.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findByOrder_Id(Long id);
}