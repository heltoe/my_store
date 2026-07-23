package com.example.my_store.payment.repository;

import com.example.my_store.account.repository.entity.AccountEntity;
import com.example.my_store.order.repository.order.entity.OrderEntity;
import com.example.my_store.order.repository.order.entity.STATE_ORDER;
import com.example.my_store.payment.repository.entity.PaymentEntity;
import com.example.my_store.payment.repository.entity.STATE_PAYMENT;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
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
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.show_sql=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
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
        AccountEntity account = persistAccount("+79255702395");
        OrderEntity order = persistOrder(account, STATE_ORDER.ACCEPTED);
        PaymentEntity payment = payment(order, STATE_PAYMENT.PENDING);

        PaymentEntity savedPayment = paymentRepository.saveAndFlush(payment);

        assertThat(savedPayment.getId()).isNotNull();
        assertThat(savedPayment.getCreatedAt()).isNotNull();
        assertThat(savedPayment.getUpdatedAt()).isNotNull();
        assertThat(savedPayment.getStatus()).isEqualTo(STATE_PAYMENT.PENDING);
    }

    @Test
    @DisplayName("findByOrder_Id возвращает платеж для заказа")
    void findByOrderId_whenPaymentExists_returnsPayment() {
        AccountEntity account = persistAccount("+79255702395");
        OrderEntity order = persistOrder(account, STATE_ORDER.ACCEPTED);
        PaymentEntity savedPayment = paymentRepository.saveAndFlush(payment(order, STATE_PAYMENT.PENDING));
        entityManager.clear();

        Optional<PaymentEntity> result = paymentRepository.findByOrder_Id(order.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedPayment.getId());
        assertThat(result.get().getOrder().getId()).isEqualTo(order.getId());
    }

    @Test
    @DisplayName("save бросает ошибку при попытке создать второй платеж для одного заказа")
    void save_whenSecondPaymentForSameOrder_throwsDataIntegrityViolation() {
        AccountEntity account = persistAccount("+79255702395");
        OrderEntity order = persistOrder(account, STATE_ORDER.ACCEPTED);
        paymentRepository.saveAndFlush(payment(order, STATE_PAYMENT.PENDING));
        entityManager.clear();

        OrderEntity reloadedOrder = entityManager.find(OrderEntity.class, order.getId());

        assertThatThrownBy(() -> paymentRepository.saveAndFlush(payment(reloadedOrder, STATE_PAYMENT.PENDING)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("deleteById удаляет платеж из базы")
    void deleteById_removesPayment() {
        AccountEntity account = persistAccount("+79255702395");
        OrderEntity order = persistOrder(account, STATE_ORDER.ACCEPTED);
        PaymentEntity savedPayment = paymentRepository.saveAndFlush(payment(order, STATE_PAYMENT.PENDING));

        paymentRepository.deleteById(savedPayment.getId());
        paymentRepository.flush();
        entityManager.clear();

        assertThat(paymentRepository.findById(savedPayment.getId())).isEmpty();
    }

    private AccountEntity persistAccount(String phoneNumber) {
        AccountEntity account = new AccountEntity();
        account.setPhoneNumber(phoneNumber);
        account.setFirstName("Владислав");
        account.setSecondName("Сергеевич");
        account.setLastName("Жулинский");
        entityManager.persist(account);
        return account;
    }

    private OrderEntity persistOrder(AccountEntity account, STATE_ORDER status) {
        OrderEntity order = new OrderEntity();
        order.setAccount(account);
        order.setStatus(status);
        entityManager.persist(order);
        return order;
    }

    private static PaymentEntity payment(OrderEntity order, STATE_PAYMENT status) {
        PaymentEntity payment = new PaymentEntity();
        payment.setOrder(order);
        payment.setStatus(status);
        return payment;
    }
}
