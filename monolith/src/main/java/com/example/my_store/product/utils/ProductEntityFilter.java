package com.example.my_store.product.utils;

import com.example.my_store.product.repository.entity.ProductEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

@Schema(description = "Фильтр для поиска товаров")
public record ProductEntityFilter(
        @Schema(description = "Подстрока в названии товара", example = "phone")
        String nameContains,
        @Schema(description = "Только активные товары (по умолчанию true)", example = "true")
        Boolean isActive
) {
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
