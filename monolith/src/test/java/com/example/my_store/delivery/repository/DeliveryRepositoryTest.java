package com.example.my_store.delivery.repository;

import com.example.my_store.account.repository.entity.AccountEntity;
import com.example.my_store.courier.repository.entity.CourierEntity;
import com.example.my_store.delivery.repository.entity.DeliveryEntity;
import com.example.my_store.order.repository.order.entity.OrderEntity;
import com.example.my_store.order.repository.order.entity.STATE_ORDER;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
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
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.show_sql=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
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
        // Arrange: готовим новую entity доставки со связанными order и courier.
        AccountEntity account = persistAccount("+79255702395");
        OrderEntity order = persistOrder(account, STATE_ORDER.WAIT_BIND_TO_COURIER);
        CourierEntity courier = persistCourier("+79161234567", true);
        DeliveryEntity delivery = delivery(order, courier);

        // Act: сохраняем доставку в тестовую PostgreSQL базу.
        DeliveryEntity savedDelivery = deliveryRepository.saveAndFlush(delivery);

        // Assert: entity получила id и даты аудита от JPA auditing.
        assertThat(savedDelivery.getId()).isNotNull();
        assertThat(savedDelivery.getCreatedAt()).isNotNull();
        assertThat(savedDelivery.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("findByOrder_Id возвращает доставку для заказа")
    void findByOrderId_whenDeliveryExists_returnsDelivery() {
        // Arrange: сохраняем доставку и очищаем persistence context.
        AccountEntity account = persistAccount("+79255702395");
        OrderEntity order = persistOrder(account, STATE_ORDER.WAIT_BIND_TO_COURIER);
        CourierEntity courier = persistCourier("+79161234567", true);
        DeliveryEntity savedDelivery = deliveryRepository.saveAndFlush(delivery(order, courier));
        entityManager.clear();

        // Act: ищем доставку через repository по id заказа.
        Optional<DeliveryEntity> result = deliveryRepository.findByOrder_Id(order.getId());

        // Assert: repository вернул доставку, связанную с нужным заказом.
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedDelivery.getId());
        assertThat(result.get().getOrder().getId()).isEqualTo(order.getId());
    }

    @Test
    @DisplayName("existsByCourier_IdAndOrder_Status возвращает true для доставки курьера в нужном статусе")
    void existsByCourierIdAndOrderStatus_whenMatchingDeliveryExists_returnsTrue() {
        // Arrange: сохраняем доставку с заказом в статусе DELIVERY_ON_THE_WAY.
        AccountEntity account = persistAccount("+79255702395");
        OrderEntity order = persistOrder(account, STATE_ORDER.DELIVERY_ON_THE_WAY);
        CourierEntity courier = persistCourier("+79161234567", true);
        deliveryRepository.saveAndFlush(delivery(order, courier));
        entityManager.clear();

        // Act: проверяем существование доставки курьера в статусе DELIVERY_ON_THE_WAY.
        boolean exists = deliveryRepository.existsByCourier_IdAndOrder_Status(
                courier.getId(),
                STATE_ORDER.DELIVERY_ON_THE_WAY
        );

        // Assert: repository нашел подходящую доставку.
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByCourier_IdAndOrder_Status возвращает false для другого статуса заказа")
    void existsByCourierIdAndOrderStatus_whenStatusDoesNotMatch_returnsFalse() {
        // Arrange: сохраняем доставку с заказом не в статусе DELIVERY_ON_THE_WAY.
        AccountEntity account = persistAccount("+79255702395");
        OrderEntity order = persistOrder(account, STATE_ORDER.PAID);
        CourierEntity courier = persistCourier("+79161234567", true);
        deliveryRepository.saveAndFlush(delivery(order, courier));
        entityManager.clear();

        // Act: проверяем наличие доставки в статусе DELIVERY_ON_THE_WAY.
        boolean exists = deliveryRepository.existsByCourier_IdAndOrder_Status(
                courier.getId(),
                STATE_ORDER.DELIVERY_ON_THE_WAY
        );

        // Assert: repository не считает delivery с другим статусом совпадением.
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("deleteById удаляет доставку из базы")
    void deleteById_removesDelivery() {
        // Arrange: сохраняем доставку, которую будем удалять.
        AccountEntity account = persistAccount("+79255702395");
        OrderEntity order = persistOrder(account, STATE_ORDER.WAIT_BIND_TO_COURIER);
        CourierEntity courier = persistCourier("+79161234567", true);
        DeliveryEntity savedDelivery = deliveryRepository.saveAndFlush(delivery(order, courier));

        // Act: удаляем доставку по id.
        deliveryRepository.deleteById(savedDelivery.getId());
        deliveryRepository.flush();
        entityManager.clear();

        // Assert: после удаления repository больше не находит доставку.
        assertThat(deliveryRepository.findById(savedDelivery.getId())).isEmpty();
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

    private CourierEntity persistCourier(String phoneNumber, Boolean isActive) {
        CourierEntity courier = new CourierEntity();
        courier.setPhoneNumber(phoneNumber);
        courier.setName("Иван");
        courier.setSecondName(null);
        courier.setLastName("Петров");
        courier.setIsActive(isActive);
        entityManager.persist(courier);
        return courier;
    }

    private static DeliveryEntity delivery(OrderEntity order, CourierEntity courier) {
        DeliveryEntity delivery = new DeliveryEntity();
        delivery.setOrder(order);
        delivery.setCourier(courier);
        delivery.setDeliveryDate(LocalDateTime.now().plusDays(1));
        delivery.setDeliveryPlace("Москва");
        delivery.setDescription("Оставить у двери");
        delivery.setLat(55.75);
        delivery.setLon(37.61);
        return delivery;
    }
}
