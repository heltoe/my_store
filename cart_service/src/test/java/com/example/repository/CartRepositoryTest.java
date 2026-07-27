package com.example.repository;

import com.example.repository.cart.CartRepository;
import com.example.repository.cart.entity.CartEntity;
import com.example.repository.cart_item.CartItemRepository;
import com.example.repository.cart_item.entity.CartItemEntity;
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
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.show_sql=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
@Import(CartRepositoryTest.AuditingConfig.class)
class CartRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:14-alpine");

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

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
    @DisplayName("save сохраняет корзину с account_id и заполняет audit-поля")
    void save_persistsCartWithAccountIdAndFillsAuditFields() {
        CartEntity cart = cart(1L);

        CartEntity savedCart = cartRepository.saveAndFlush(cart);

        assertThat(savedCart.getId()).isNotNull();
        assertThat(savedCart.getCreatedAt()).isNotNull();
        assertThat(savedCart.getUpdatedAt()).isNotNull();
        assertThat(savedCart.getAccount_id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findById возвращает корзину с сохраненным account_id")
    void findById_whenCartExists_returnsCartWithAccountId() {
        CartEntity savedCart = cartRepository.saveAndFlush(cart(2L));
        entityManager.clear();

        Optional<CartEntity> result = cartRepository.findById(savedCart.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getAccount_id()).isEqualTo(2L);
    }

    @Test
    @DisplayName("save сохраняет позицию корзины со связями cart и product_id")
    void save_persistsCartItemWithCartAndProductId() {
        CartEntity cart = cartRepository.saveAndFlush(cart(1L));
        CartItemEntity cartItem = cartItem(cart, 10L, 3);

        CartItemEntity savedCartItem = cartItemRepository.saveAndFlush(cartItem);
        entityManager.clear();

        Optional<CartItemEntity> result = cartItemRepository.findById(savedCartItem.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getCart().getId()).isEqualTo(cart.getId());
        assertThat(result.get().getProductId()).isEqualTo(10L);
        assertThat(result.get().getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("cart хранит список позиций корзины")
    void findById_whenCartHasItems_returnsCartWithProducts() {
        CartEntity cart = cartRepository.saveAndFlush(cart(1L));
        CartItemEntity cartItem = cartItemRepository.saveAndFlush(cartItem(cart, 20L, 2));
        entityManager.clear();

        CartEntity result = cartRepository.findById(cart.getId()).orElseThrow();

        assertThat(result.getProducts())
                .extracting(CartItemEntity::getId)
                .containsExactly(cartItem.getId());
    }

    @Test
    @DisplayName("deleteById удаляет позицию корзины из базы")
    void deleteById_removesCartItem() {
        CartEntity cart = cartRepository.saveAndFlush(cart(1L));
        CartItemEntity savedCartItem = cartItemRepository.saveAndFlush(cartItem(cart, 30L, 1));

        cartItemRepository.deleteById(savedCartItem.getId());
        cartItemRepository.flush();
        entityManager.clear();

        assertThat(cartItemRepository.findById(savedCartItem.getId())).isEmpty();
    }

    private static CartEntity cart(Long accountId) {
        CartEntity cart = new CartEntity();
        cart.setAccount_id(accountId);
        return cart;
    }

    private static CartItemEntity cartItem(CartEntity cart, Long productId, Integer quantity) {
        CartItemEntity cartItem = new CartItemEntity();
        cartItem.setCart(cart);
        cartItem.setProductId(productId);
        cartItem.setQuantity(quantity);
        return cartItem;
    }

    @EnableJpaAuditing
    @TestConfiguration
    static class AuditingConfig {
    }
}
