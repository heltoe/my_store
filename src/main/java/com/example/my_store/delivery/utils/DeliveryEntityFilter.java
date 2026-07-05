package com.example.my_store.delivery.utils;

import com.example.my_store.delivery.repository.entity.DeliveryEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public record DeliveryEntityFilter(Long id, LocalDateTime deliveryDate) {
    public Specification<DeliveryEntity> toSpecification() {
        return idSpec()
                .and(deliveryDateSpec());
    }

    private Specification<DeliveryEntity> idSpec() {
        return ((root, query, cb) -> id != null
                ? cb.equal(root.get("id"), id)
                : null);
    }

    private Specification<DeliveryEntity> deliveryDateSpec() {
        return ((root, query, cb) -> deliveryDate != null
                ? cb.equal(root.get("deliveryDate"), deliveryDate)
                : null);
    }
}