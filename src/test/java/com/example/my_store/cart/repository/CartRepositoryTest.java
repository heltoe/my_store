package com.example.my_store.cart.repository;

import com.example.my_store.account.repository.AccountRepository;
import com.example.my_store.account.repository.entity.AccountEntity;
import com.example.my_store.cart.repository.cart.CartRepository;
import com.example.my_store.cart.repository.cart.entity.CartEntity;
import com.example.my_store.cart.repository.cart_item.CartItemRepository;
import com.example.my_store.cart.repository.cart_item.entity.CartItemEntity;
import com.example.my_store.product.repository.ProductRepository;
import com.example.my_store.product.repository.entity.ProductEntity;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.config.import=optional:file:.env[.properties]",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.show_sql=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class CartRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:14-alpine");

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProductRepository productRepository;

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
    @DisplayName("save сохраняет корзину с аккаунтом и заполняет audit-поля")
    void save_persistsCartWithAccountAndFillsAuditFields() {
        // Arrange: сохраняем аккаунт и создаем корзину с обязательной связью.
        AccountEntity account = accountRepository.saveAndFlush(account());
        CartEntity cart = cart(account);

        // Act: сохраняем корзину в тестовую PostgreSQL базу.
        CartEntity savedCart = cartRepository.saveAndFlush(cart);

        // Assert: корзина получила id, audit-поля и связь с аккаунтом.
        assertThat(savedCart.getId()).isNotNull();
        assertThat(savedCart.getCreatedAt()).isNotNull();
        assertThat(savedCart.getUpdatedAt()).isNotNull();
        assertThat(savedCart.getAccount().getId()).isEqualTo(account.getId());
    }

    @Test
    @DisplayName("findById возвращает корзину с сохраненным аккаунтом")
    void findById_whenCartExists_returnsCartWithAccount() {
        // Arrange: сохраняем аккаунт и корзину, затем очищаем persistence context.
        AccountEntity account = accountRepository.saveAndFlush(account());
        CartEntity savedCart = cartRepository.saveAndFlush(cart(account));
        entityManager.clear();

        // Act: ищем корзину через repository по id.
        Optional<CartEntity> result = cartRepository.findById(savedCart.getId());

        // Assert: repository вернул корзину со связью на аккаунт.
        assertThat(result).isPresent();
        assertThat(result.get().getAccount().getId()).isEqualTo(account.getId());
    }

    @Test
    @DisplayName("save сохраняет позицию корзины со связями cart и product")
    void save_persistsCartItemWithCartAndProduct() {
        // Arrange: сохраняем аккаунт, корзину, продукт и создаем позицию корзины.
        AccountEntity account = accountRepository.saveAndFlush(account());
        CartEntity cart = cartRepository.saveAndFlush(cart(account));
        ProductEntity product = productRepository.saveAndFlush(product());
        CartItemEntity cartItem = cartItem(cart, product, 3);

        // Act: сохраняем позицию корзины в тестовую PostgreSQL базу.
        CartItemEntity savedCartItem = cartItemRepository.saveAndFlush(cartItem);
        entityManager.clear();

        // Assert: repository может найти позицию со связями cart/product и количеством.
        Optional<CartItemEntity> result = cartItemRepository.findById(savedCartItem.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getCart().getId()).isEqualTo(cart.getId());
        assertThat(result.get().getProduct().getId()).isEqualTo(product.getId());
        assertThat(result.get().getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("cart хранит список позиций корзины")
    void findById_whenCartHasItems_returnsCartWithProducts() {
        // Arrange: сохраняем корзину и связанную с ней позицию.
        AccountEntity account = accountRepository.saveAndFlush(account());
        CartEntity cart = cartRepository.saveAndFlush(cart(account));
        ProductEntity product = productRepository.saveAndFlush(product());
        CartItemEntity cartItem = cartItemRepository.saveAndFlush(cartItem(cart, product, 2));
        entityManager.clear();

        // Act: ищем корзину через repository по id.
        CartEntity result = cartRepository.findById(cart.getId()).orElseThrow();

        // Assert: связь OneToMany возвращает сохраненную позицию корзины.
        assertThat(result.getProducts())
                .extracting(CartItemEntity::getId)
                .containsExactly(cartItem.getId());
    }

    @Test
    @DisplayName("deleteById удаляет позицию корзины из базы")
    void deleteById_removesCartItem() {
        // Arrange: сохраняем позицию корзины, которую будем удалять.
        AccountEntity account = accountRepository.saveAndFlush(account());
        CartEntity cart = cartRepository.saveAndFlush(cart(account));
        ProductEntity product = productRepository.saveAndFlush(product());
        CartItemEntity savedCartItem = cartItemRepository.saveAndFlush(cartItem(cart, product, 1));

        // Act: удаляем позицию корзины по id.
        cartItemRepository.deleteById(savedCartItem.getId());
        cartItemRepository.flush();
        entityManager.clear();

        // Assert: после удаления repository больше не находит позицию корзины.
        assertThat(cartItemRepository.findById(savedCartItem.getId())).isEmpty();
    }

    private static AccountEntity account() {
        AccountEntity account = new AccountEntity();
        account.setPhoneNumber("+79255702395");
        account.setFirstName("Владислав");
        account.setSecondName("Сергеевич");
        account.setLastName("Жулинский");
        return account;
    }

    private static ProductEntity product() {
        ProductEntity product = new ProductEntity();
        product.setName("Ноутбук");
        product.setNormalizedName("ноутбук");
        product.setDescription("Игровой ноутбук");
        product.setPrice(100_000.0);
        product.setQuantity(5);
        product.setIsActive(true);
        return product;
    }

    private static CartEntity cart(AccountEntity account) {
        CartEntity cart = new CartEntity();
        cart.setAccount(account);
        return cart;
    }

    private static CartItemEntity cartItem(CartEntity cart, ProductEntity product, Integer quantity) {
        CartItemEntity cartItem = new CartItemEntity();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        return cartItem;
    }
}
