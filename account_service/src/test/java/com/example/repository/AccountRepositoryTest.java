package com.example.repository;

import com.example.repository.entity.AccountEntity;
import com.example.utils.AccountEntityFilter;
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
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.show_sql=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
@Import(AccountRepositoryTest.AuditingConfig.class)
class AccountRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:14-alpine");

    @Autowired
    private AccountRepository accountRepository;

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
    @DisplayName("save сохраняет аккаунт и заполняет id и audit-поля")
    void save_persistsAccountAndFillsAuditFields() {
        // Arrange: готовим новую entity аккаунта.
        AccountEntity account = account(
                "+79255702395",
                "Владислав",
                "Сергеевич",
                "Жулинский"
        );

        // Act: сохраняем аккаунт в тестовую PostgreSQL базу.
        AccountEntity savedAccount = accountRepository.saveAndFlush(account);

        // Assert: entity получила id и даты аудита от JPA auditing.
        assertThat(savedAccount.getId()).isNotNull();
        assertThat(savedAccount.getCreatedAt()).isNotNull();
        assertThat(savedAccount.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("findById возвращает сохраненный аккаунт")
    void findById_whenAccountExists_returnsAccount() {
        // Arrange: сохраняем аккаунт и очищаем persistence context.
        AccountEntity savedAccount = accountRepository.saveAndFlush(account(
                "+79255702395",
                "Владислав",
                "Сергеевич",
                "Жулинский"
        ));
        entityManager.clear();

        // Act: ищем аккаунт через repository по id.
        Optional<AccountEntity> result = accountRepository.findById(savedAccount.getId());

        // Assert: repository вернул сохраненную запись из базы.
        assertThat(result).isPresent();
        assertThat(result.get().getPhoneNumber()).isEqualTo("+79255702395");
        assertThat(result.get().getFirstName()).isEqualTo("Владислав");
        assertThat(result.get().getSecondName()).isEqualTo("Сергеевич");
        assertThat(result.get().getLastName()).isEqualTo("Жулинский");
    }

    @Test
    @DisplayName("findAll со Specification фильтрует аккаунты по телефону")
    void findAll_withPhoneNumberSpecification_returnsMatchingAccounts() {
        // Arrange: сохраняем два аккаунта с разными телефонами.
        AccountEntity matchingAccount = accountRepository.save(account(
                "+79255702395",
                "Владислав",
                "Сергеевич",
                "Жулинский"
        ));
        accountRepository.save(account(
                "+79161234567",
                "Иван",
                null,
                "Петров"
        ));
        accountRepository.flush();
        entityManager.clear();

        AccountEntityFilter filter = new AccountEntityFilter("925", null, null, null);

        // Act: ищем аккаунты через Specification из фильтра.
        var result = accountRepository.findAll(filter.toSpecification());

        // Assert: в результате остался только аккаунт с подходящим телефоном.
        assertThat(result)
                .extracting(AccountEntity::getId)
                .containsExactly(matchingAccount.getId());
    }

    @Test
    @DisplayName("findAll со Specification фильтрует аккаунты по ФИО без учета регистра")
    void findAll_withNameSpecifications_returnsMatchingAccountsIgnoringCase() {
        // Arrange: сохраняем аккаунты и готовим фильтр по частям ФИО.
        AccountEntity matchingAccount = accountRepository.save(account(
                "+79255702395",
                "Владислав",
                "Сергеевич",
                "Жулинский"
        ));
        accountRepository.save(account(
                "+79161234567",
                "Иван",
                "Иванович",
                "Петров"
        ));
        accountRepository.flush();
        entityManager.clear();

        AccountEntityFilter filter = new AccountEntityFilter(null, "влад", "жул", "серг");

        // Act: применяем Specification к repository.
        var result = accountRepository.findAll(filter.toSpecification());

        // Assert: фильтр нашел аккаунт по firstName, lastName и secondName.
        assertThat(result)
                .extracting(AccountEntity::getId)
                .containsExactly(matchingAccount.getId());
    }

    @Test
    @DisplayName("deleteById удаляет аккаунт из базы")
    void deleteById_removesAccount() {
        // Arrange: сохраняем аккаунт, который будем удалять.
        AccountEntity savedAccount = accountRepository.saveAndFlush(account(
                "+79255702395",
                "Владислав",
                "Сергеевич",
                "Жулинский"
        ));

        // Act: удаляем аккаунт по id.
        accountRepository.deleteById(savedAccount.getId());
        accountRepository.flush();
        entityManager.clear();

        // Assert: после удаления repository больше не находит аккаунт.
        assertThat(accountRepository.findById(savedAccount.getId())).isEmpty();
    }

    private static AccountEntity account(String phoneNumber, String firstName, String secondName, String lastName) {
        AccountEntity account = new AccountEntity();
        account.setPhoneNumber(phoneNumber);
        account.setFirstName(firstName);
        account.setSecondName(secondName);
        account.setLastName(lastName);
        return account;
    }

    @EnableJpaAuditing
    @TestConfiguration
    static class AuditingConfig {
    }
}
