package com.example.repository;

import com.example.repository.entity.PaymentEntity;
import com.example.repository.entity.STATE_PAYMENT;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = {
        "spring.config.import=optional:file:.env[.properties]",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.show_sql=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
@Import(PaymentRepositoryTest.AuditingConfig.class)
class PaymentRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:14-alpine");

    @Autowired
    private PaymentRepository paymentRepository;

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
    @DisplayName("save сохраняет платеж и заполняет id и audit-поля")
    void save_persistsPaymentAndFillsAuditFields() {
        PaymentEntity payment = payment(10L, STATE_PAYMENT.PENDING);

        PaymentEntity savedPayment = paymentRepository.saveAndFlush(payment);

        assertThat(savedPayment.getId()).isNotNull();
        assertThat(savedPayment.getCreatedAt()).isNotNull();
        assertThat(savedPayment.getUpdatedAt()).isNotNull();
        assertThat(savedPayment.getStatus()).isEqualTo(STATE_PAYMENT.PENDING);
        assertThat(savedPayment.getOrderId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("findByOrderId возвращает платеж для заказа")
    void findByOrderId_whenPaymentExists_returnsPayment() {
        PaymentEntity savedPayment = paymentRepository.saveAndFlush(payment(20L, STATE_PAYMENT.PENDING));
        entityManager.clear();

        Optional<PaymentEntity> result = paymentRepository.findByOrderId(20L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedPayment.getId());
        assertThat(result.get().getOrderId()).isEqualTo(20L);
    }

    @Test
    @DisplayName("save бросает ошибку при попытке создать второй платеж для одного заказа")
    void save_whenSecondPaymentForSameOrder_throwsDataIntegrityViolation() {
        paymentRepository.saveAndFlush(payment(30L, STATE_PAYMENT.PENDING));
        entityManager.clear();

        assertThatThrownBy(() -> paymentRepository.saveAndFlush(payment(30L, STATE_PAYMENT.PENDING)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("deleteById удаляет платеж из базы")
    void deleteById_removesPayment() {
        PaymentEntity savedPayment = paymentRepository.saveAndFlush(payment(40L, STATE_PAYMENT.PENDING));

        paymentRepository.deleteById(savedPayment.getId());
        paymentRepository.flush();
        entityManager.clear();

        assertThat(paymentRepository.findById(savedPayment.getId())).isEmpty();
    }

    private static PaymentEntity payment(Long orderId, STATE_PAYMENT status) {
        PaymentEntity payment = new PaymentEntity();
        payment.setOrderId(orderId);
        payment.setStatus(status);
        return payment;
    }

    @EnableJpaAuditing
    @TestConfiguration
    static class AuditingConfig {
    }
}
