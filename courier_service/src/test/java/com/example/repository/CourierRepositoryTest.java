package com.example.repository;

import com.example.repository.entity.CourierEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.config.import=optional:file:.env[.properties]",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.show_sql=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
@Import(CourierRepositoryTest.AuditingConfig.class)
class CourierRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:14-alpine");

    @Autowired
    private CourierRepository courierRepository;

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
    @DisplayName("save сохраняет курьера и заполняет id и audit-поля")
    void save_persistsCourierAndFillsAuditFields() {
        // Arrange: готовим новую entity курьера.
        CourierEntity courier = courier(
                "+79255702395",
                "Владислав",
                "Сергеевич",
                "Жулинский",
                true
        );

        // Act: сохраняем курьера в тестовую PostgreSQL базу.
        CourierEntity savedCourier = courierRepository.saveAndFlush(courier);

        // Assert: entity получила id и даты аудита от JPA auditing.
        assertThat(savedCourier.getId()).isNotNull();
        assertThat(savedCourier.getCreatedAt()).isNotNull();
        assertThat(savedCourier.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("findById возвращает сохраненного курьера")
    void findById_whenCourierExists_returnsCourier() {
        // Arrange: сохраняем курьера и очищаем persistence context.
        CourierEntity savedCourier = courierRepository.saveAndFlush(courier(
                "+79255702395",
                "Владислав",
                "Сергеевич",
                "Жулинский",
                true
        ));
        entityManager.clear();

        // Act: ищем курьера через repository по id.
        Optional<CourierEntity> result = courierRepository.findById(savedCourier.getId());

        // Assert: repository вернул сохраненную запись из базы.
        assertThat(result).isPresent();
        assertThat(result.get().getPhoneNumber()).isEqualTo("+79255702395");
        assertThat(result.get().getName()).isEqualTo("Владислав");
        assertThat(result.get().getSecondName()).isEqualTo("Сергеевич");
        assertThat(result.get().getLastName()).isEqualTo("Жулинский");
        assertThat(result.get().getIsActive()).isTrue();
    }

    @Test
    @DisplayName("existsByPhoneNumber возвращает true, если телефон занят")
    void existsByPhoneNumber_whenPhoneNumberExists_returnsTrue() {
        // Arrange: сохраняем курьера с проверяемым телефоном.
        courierRepository.saveAndFlush(courier(
                "+79255702395",
                "Владислав",
                "Сергеевич",
                "Жулинский",
                true
        ));
        entityManager.clear();

        // Act: проверяем существование курьера по телефону.
        boolean exists = courierRepository.existsByPhoneNumber("+79255702395");

        // Assert: repository нашел запись с таким телефоном.
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByPhoneNumberAndIdNot игнорирует текущего курьера")
    void existsByPhoneNumberAndIdNot_whenPhoneBelongsToSameCourier_returnsFalse() {
        // Arrange: сохраняем курьера, телефон которого будем проверять относительно его же id.
        CourierEntity savedCourier = courierRepository.saveAndFlush(courier(
                "+79255702395",
                "Владислав",
                "Сергеевич",
                "Жулинский",
                true
        ));
        entityManager.clear();

        // Act: проверяем, занят ли телефон другим курьером.
        boolean exists = courierRepository.existsByPhoneNumberAndIdNot(
                "+79255702395",
                savedCourier.getId()
        );

        // Assert: repository не считает текущего курьера конфликтом.
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByPhoneNumberAndIdNot находит другого курьера с таким телефоном")
    void existsByPhoneNumberAndIdNot_whenPhoneBelongsToAnotherCourier_returnsTrue() {
        // Arrange: сохраняем двух курьеров с разными телефонами.
        CourierEntity firstCourier = courierRepository.save(courier(
                "+79255702395",
                "Владислав",
                "Сергеевич",
                "Жулинский",
                true
        ));
        CourierEntity secondCourier = courierRepository.save(courier(
                "+79161234567",
                "Иван",
                null,
                "Петров",
                true
        ));
        courierRepository.flush();
        entityManager.clear();

        // Act: проверяем телефон первого курьера относительно id второго курьера.
        boolean exists = courierRepository.existsByPhoneNumberAndIdNot(
                firstCourier.getPhoneNumber(),
                secondCourier.getId()
        );

        // Assert: repository нашел другого курьера с таким телефоном.
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("deleteById удаляет курьера из базы")
    void deleteById_removesCourier() {
        // Arrange: сохраняем курьера, которого будем удалять.
        CourierEntity savedCourier = courierRepository.saveAndFlush(courier(
                "+79255702395",
                "Владислав",
                "Сергеевич",
                "Жулинский",
                true
        ));

        // Act: удаляем курьера по id.
        courierRepository.deleteById(savedCourier.getId());
        courierRepository.flush();
        entityManager.clear();

        // Assert: после удаления repository больше не находит курьера.
        assertThat(courierRepository.findById(savedCourier.getId())).isEmpty();
    }

    private static CourierEntity courier(String phoneNumber, String name, String secondName, String lastName, Boolean isActive) {
        CourierEntity courier = new CourierEntity();
        courier.setPhoneNumber(phoneNumber);
        courier.setName(name);
        courier.setSecondName(secondName);
        courier.setLastName(lastName);
        courier.setIsActive(isActive);
        return courier;
    }

    @EnableJpaAuditing
    @TestConfiguration
    static class AuditingConfig {
    }
}
