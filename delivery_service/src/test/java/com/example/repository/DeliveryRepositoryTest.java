package com.example.repository;

import com.example.repository.entity.DeliveryEntity;
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

import java.time.LocalDateTime;
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
@Import(DeliveryRepositoryTest.AuditingConfig.class)
class DeliveryRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:14-alpine");

    @Autowired
    private DeliveryRepository deliveryRepository;

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
    @DisplayName("save сохраняет доставку и заполняет id и audit-поля")
    void save_persistsDeliveryAndFillsAuditFields() {
        DeliveryEntity delivery = delivery(10L, 20L);

        DeliveryEntity savedDelivery = deliveryRepository.saveAndFlush(delivery);

        assertThat(savedDelivery.getId()).isNotNull();
        assertThat(savedDelivery.getCreatedAt()).isNotNull();
        assertThat(savedDelivery.getUpdatedAt()).isNotNull();
        assertThat(savedDelivery.getOrderId()).isEqualTo(10L);
        assertThat(savedDelivery.getCourierId()).isEqualTo(20L);
    }

    @Test
    @DisplayName("findByOrderId возвращает доставку для заказа")
    void findByOrderId_whenDeliveryExists_returnsDelivery() {
        DeliveryEntity savedDelivery = deliveryRepository.saveAndFlush(delivery(11L, 21L));
        entityManager.clear();

        Optional<DeliveryEntity> result = deliveryRepository.findByOrderId(11L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedDelivery.getId());
        assertThat(result.get().getOrderId()).isEqualTo(11L);
    }

    @Test
    @DisplayName("findByCourierId возвращает доставки курьера")
    void findByCourierId_whenDeliveriesExist_returnsDeliveries() {
        deliveryRepository.saveAndFlush(delivery(12L, 22L));
        deliveryRepository.saveAndFlush(delivery(13L, 22L));
        entityManager.clear();

        var result = deliveryRepository.findByCourierId(22L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(DeliveryEntity::getCourierId).containsOnly(22L);
    }

    @Test
    @DisplayName("deleteById удаляет доставку из базы")
    void deleteById_removesDelivery() {
        DeliveryEntity savedDelivery = deliveryRepository.saveAndFlush(delivery(14L, 23L));

        deliveryRepository.deleteById(savedDelivery.getId());
        deliveryRepository.flush();
        entityManager.clear();

        assertThat(deliveryRepository.findById(savedDelivery.getId())).isEmpty();
    }

    private static DeliveryEntity delivery(Long orderId, Long courierId) {
        DeliveryEntity delivery = new DeliveryEntity();
        delivery.setOrderId(orderId);
        delivery.setCourierId(courierId);
        delivery.setDeliveryDate(LocalDateTime.now().plusDays(1));
        delivery.setDeliveryPlace("Москва");
        delivery.setDescription("Оставить у двери");
        delivery.setLat(55.75);
        delivery.setLon(37.61);
        return delivery;
    }

    @EnableJpaAuditing
    @TestConfiguration
    static class AuditingConfig {
    }
}
