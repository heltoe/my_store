package com.example.my_store.product.utils;

import com.example.my_store.product.repository.entity.ProductEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public record ProductEntityFilter(String nameContains, Boolean isActive) {
    public Specification<ProductEntity> toSpecification() {
        return nameContainsSpec()
                .and(isActiveSpec());
    }

    private Specification<ProductEntity> nameContainsSpec() {
        return ((root, query, cb) -> StringUtils.hasText(nameContains)
                ? cb.like(cb.lower(root.get("name")), "%" + nameContains.toLowerCase() + "%")
                : null);
    }

    private Specification<ProductEntity> isActiveSpec() {
        boolean activeOnly = isActive == null || isActive;
        return (root, query, cb) -> cb.equal(root.get("isActive"), activeOnly);
    }
}