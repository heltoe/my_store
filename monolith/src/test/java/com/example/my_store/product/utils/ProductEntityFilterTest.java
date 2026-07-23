package com.example.my_store.product.utils;

import com.example.my_store.product.repository.ProductRepository;
import com.example.my_store.product.repository.entity.ProductEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.config.import=optional:file:.env[.properties]",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.show_sql=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class ProductEntityFilterTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:14-alpine");

    @Autowired
    private ProductRepository productRepository;

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @Test
    @DisplayName("без isActive возвращает только активные продукты")
    void toSpecification_whenIsActiveIsNull_returnsOnlyActiveProducts() {
        ProductEntity active = productRepository.saveAndFlush(activeProduct("Ноутбук", "ноутбук"));
        productRepository.saveAndFlush(inactiveProduct("Мышь", "мышь"));

        Specification<ProductEntity> spec = new ProductEntityFilter(null, null).toSpecification();
        List<ProductEntity> result = productRepository.findAll(spec);

        assertThat(result).extracting(ProductEntity::getId).containsExactly(active.getId());
    }

    @Test
    @DisplayName("isActive=true возвращает только активные продукты")
    void toSpecification_whenIsActiveIsTrue_returnsOnlyActiveProducts() {
        ProductEntity active = productRepository.saveAndFlush(activeProduct("Ноутбук", "ноутбук"));
        productRepository.saveAndFlush(inactiveProduct("Мышь", "мышь"));

        Specification<ProductEntity> spec = new ProductEntityFilter(null, true).toSpecification();
        List<ProductEntity> result = productRepository.findAll(spec);

        assertThat(result).extracting(ProductEntity::getId).containsExactly(active.getId());
    }

    @Test
    @DisplayName("isActive=false возвращает только неактивные продукты")
    void toSpecification_whenIsActiveIsFalse_returnsOnlyInactiveProducts() {
        productRepository.saveAndFlush(activeProduct("Ноутбук", "ноутбук"));
        ProductEntity inactive = productRepository.saveAndFlush(inactiveProduct("Мышь", "мышь"));

        Specification<ProductEntity> spec = new ProductEntityFilter(null, false).toSpecification();
        List<ProductEntity> result = productRepository.findAll(spec);

        assertThat(result).extracting(ProductEntity::getId).containsExactly(inactive.getId());
    }

    @Test
    @DisplayName("nameContains фильтрует по части имени без учета регистра")
    void toSpecification_whenNameContains_filtersByName() {
        ProductEntity laptop = productRepository.saveAndFlush(activeProduct("Игровой Ноутбук", "игровой ноутбук"));
        productRepository.saveAndFlush(activeProduct("Мышь", "мышь"));

        Specification<ProductEntity> spec = new ProductEntityFilter("ноут", true).toSpecification();
        List<ProductEntity> result = productRepository.findAll(spec);

        assertThat(result).extracting(ProductEntity::getId).containsExactly(laptop.getId());
    }

    private static ProductEntity activeProduct(String name, String normalizedName) {
        ProductEntity product = new ProductEntity();
        product.setName(name);
        product.setNormalizedName(normalizedName);
        product.setDescription("Описание");
        product.setPrice(1_000.0);
        product.setQuantity(1);
        product.setIsActive(true);
        return product;
    }

    private static ProductEntity inactiveProduct(String name, String normalizedName) {
        ProductEntity product = activeProduct(name, normalizedName);
        product.setIsActive(false);
        return product;
    }
}
