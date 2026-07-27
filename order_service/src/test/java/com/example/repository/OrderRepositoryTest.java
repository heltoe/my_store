package com.example.repository;

import com.example.common_lib.dto.STATE_ORDER;
import com.example.repository.order.OrderRepository;
import com.example.repository.order.entity.OrderEntity;
import com.example.repository.order_item.OrderItemRepository;
import com.example.repository.order_item.entity.OrderItemEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.config.import=optional:file:.env[.properties]",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.show_sql=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
@Import(OrderRepositoryTest.AuditingConfig.class)
class OrderRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:14-alpine");

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private EntityManager entityManager;

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @Test
    @DisplayName("save сохраняет заказ и заполняет id и audit-поля")
    void save_persistsOrderAndFillsAuditFields() {
        OrderEntity order = order(1L, STATE_ORDER.CREATED);

        OrderEntity savedOrder = orderRepository.saveAndFlush(order);

        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getCreatedAt()).isNotNull();
        assertThat(savedOrder.getUpdatedAt()).isNotNull();
        assertThat(savedOrder.getAccountId()).isEqualTo(1L);
        assertThat(savedOrder.getStatus()).isEqualTo(STATE_ORDER.CREATED);
    }

    @Test
    @DisplayName("findById возвращает сохраненный заказ")
    void findById_whenOrderExists_returnsOrder() {
        OrderEntity savedOrder = orderRepository.saveAndFlush(order(2L, STATE_ORDER.ACCEPTED));
        entityManager.clear();

        Optional<OrderEntity> result = orderRepository.findById(savedOrder.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getAccountId()).isEqualTo(2L);
        assertThat(result.get().getStatus()).isEqualTo(STATE_ORDER.ACCEPTED);
    }

    @Test
    @DisplayName("save сохраняет позицию заказа со связью order и product_id")
    void save_persistsOrderItemWithOrderAndProductId() {
        OrderEntity order = orderRepository.saveAndFlush(order(1L, STATE_ORDER.CREATED));
        OrderItemEntity orderItem = orderItem(order, 10L, 100.0, 2);

        OrderItemEntity savedItem = orderItemRepository.saveAndFlush(orderItem);
        entityManager.clear();

        Optional<OrderItemEntity> result = orderItemRepository.findById(savedItem.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getOrder().getId()).isEqualTo(order.getId());
        assertThat(result.get().getProductId()).isEqualTo(10L);
        assertThat(result.get().getPrice()).isEqualTo(100.0);
        assertThat(result.get().getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("deleteById удаляет заказ из базы")
    void deleteById_removesOrder() {
        OrderEntity savedOrder = orderRepository.saveAndFlush(order(3L, STATE_ORDER.CREATED));

        orderRepository.deleteById(savedOrder.getId());
        orderRepository.flush();
        entityManager.clear();

        assertThat(orderRepository.findById(savedOrder.getId())).isEmpty();
    }

    private static OrderEntity order(Long accountId, STATE_ORDER status) {
        OrderEntity order = new OrderEntity();
        order.setAccountId(accountId);
        order.setStatus(status);
        return order;
    }

    private static OrderItemEntity orderItem(OrderEntity order, Long productId, Double price, Integer quantity) {
        OrderItemEntity item = new OrderItemEntity();
        item.setOrder(order);
        item.setProductId(productId);
        item.setPrice(price);
        item.setQuantity(quantity);
        return item;
    }

    @EnableJpaAuditing
    @TestConfiguration
    static class AuditingConfig {
    }
}
