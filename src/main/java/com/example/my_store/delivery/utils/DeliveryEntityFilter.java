package com.example.my_store.delivery.utils;

import com.example.my_store.delivery.repository.entity.DeliveryEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

@Schema(description = "Фильтр для поиска доставок")
public record DeliveryEntityFilter(
        @Schema(description = "Идентификатор доставки", example = "1")
        Long id,
        @Schema(description = "Дата и время доставки", example = "2026-07-20T12:00:00")
        LocalDateTime deliveryDate
) {
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
