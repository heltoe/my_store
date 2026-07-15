package com.example.my_store.cart.service;

import com.example.my_store.account.repository.AccountRepository;
import com.example.my_store.account.repository.entity.AccountEntity;
import com.example.my_store.cart.controller.dto.cart.CreateCartDto;
import com.example.my_store.cart.controller.dto.cart.GetCartDto;
import com.example.my_store.cart.controller.dto.cart_item.CreateCartItemDto;
import com.example.my_store.cart.controller.dto.cart_item.UpdateCartItemDto;
import com.example.my_store.cart.repository.cart.CartRepository;
import com.example.my_store.cart.repository.cart.entity.CartEntity;
import com.example.my_store.cart.repository.cart_item.CartItemRepository;
import com.example.my_store.cart.repository.cart_item.entity.CartItemEntity;
import com.example.my_store.cart.utils.CartEntityMapper;
import com.example.my_store.cart.utils.CartItemEntityMapper;
import com.example.my_store.product.repository.entity.ProductEntity;
import com.example.my_store.product.service.ProductService;
import com.example.my_store.utils.exception.CommonEntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    private static final Long CART_ID = 1L;
    private static final Long ACCOUNT_ID = 2L;
    private static final Long PRODUCT_ID = 3L;
    private static final Long CART_ITEM_ID = 4L;

    @Mock
    private CartEntityMapper cartEntityMapper;

    @Mock
    private CartItemEntityMapper cartItemEntityMapper;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ProductService productService;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private static AccountEntity accountEntity() {
        AccountEntity account = new AccountEntity();
        account.setId(ACCOUNT_ID);
        account.setPhoneNumber("+79255702395");
        account.setFirstName("Владислав");
        account.setLastName("Жулинский");
        return account;
    }

    private static ProductEntity productEntity() {
        ProductEntity product = new ProductEntity();
        product.setId(PRODUCT_ID);
        product.setName("Ноутбук");
        product.setDescription("Игровой ноутбук");
        product.setPrice(100_000.0);
        product.setQuantity(5);
        product.setIsActive(true);
        return product;
    }

    private static CartEntity cartEntity() {
        CartEntity cart = new CartEntity();
        cart.setId(CART_ID);
        cart.setAccount(accountEntity());
        return cart;
    }

    private static CartItemEntity cartItemEntity(CartEntity cart, ProductEntity product, Integer quantity) {
        CartItemEntity cartItem = new CartItemEntity();
        cartItem.setId(CART_ITEM_ID);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        return cartItem;
    }

    private static GetCartDto getCartDto() {
        return new GetCartDto(
                new Date(),
                LocalDateTime.now(),
                CART_ID
        );
    }

    @Test
    @DisplayName("getAll возвращает страницу корзин, преобразованную в DTO")
    void getAll_returnsMappedPage() {
        // Arrange: repository возвращает страницу entity, mapper превращает entity в DTO.
        CartEntity cart = cartEntity();
        GetCartDto dto = getCartDto();
        Pageable pageable = PageRequest.of(0, 10);

        when(cartRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(cart)));
        when(cartEntityMapper.convertToGetCartDto(cart)).thenReturn(dto);

        // Act: вызываем метод сервиса, который тестируем.
        var result = cartService.getAll(pageable);

        // Assert: получили ожидаемую страницу DTO и проверили вызовы зависимостей.
        assertThat(result.getContent()).containsExactly(dto);
        verify(cartRepository).findAll(pageable);
        verify(cartEntityMapper).convertToGetCartDto(cart);
    }

    @Test
    @DisplayName("getOne возвращает DTO, если корзина найдена")
    void getOne_whenCartExists_returnsDto() {
        // Arrange: repository находит корзину, mapper превращает entity в DTO.
        CartEntity cart = cartEntity();
        GetCartDto dto = getCartDto();

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));
        when(cartEntityMapper.convertToGetCartDto(cart)).thenReturn(dto);

        // Act: запрашиваем корзину по id.
        GetCartDto result = cartService.getOne(CART_ID);

        // Assert: сервис вернул DTO и обратился к нужным зависимостям.
        assertThat(result).isEqualTo(dto);
        verify(cartRepository).findById(CART_ID);
        verify(cartEntityMapper).convertToGetCartDto(cart);
    }

    @Test
    @DisplayName("getOne бросает 404, если корзина не найдена")
    void getOne_whenCartDoesNotExist_throwsNotFound() {
        // Arrange: repository не находит корзину.
        when(cartRepository.findById(CART_ID)).thenReturn(Optional.empty());

        // Act + Assert: вызов сервиса должен завершиться ошибкой ненайденной entity.
        assertThatThrownBy(() -> cartService.getOne(CART_ID))
                .isInstanceOf(CommonEntityNotFoundException.class)
                .hasMessage("Cart with id `1` not found");

        // Assert: сервис действительно пытался найти корзину по id.
        verify(cartRepository).findById(CART_ID);
    }

    @Test
    @DisplayName("getMany возвращает список корзин, преобразованный в DTO")
    void getMany_returnsMappedDtos() {
        // Arrange: repository возвращает список entity, mapper превращает их в DTO.
        CartEntity cart = cartEntity();
        GetCartDto dto = getCartDto();
        List<Long> ids = List.of(CART_ID);

        when(cartRepository.findAllById(ids)).thenReturn(List.of(cart));
        when(cartEntityMapper.convertToGetCartDto(cart)).thenReturn(dto);

        // Act: запрашиваем несколько корзин по id.
        List<GetCartDto> result = cartService.getMany(ids);

        // Assert: получили ожидаемый список DTO и проверили вызовы.
        assertThat(result).containsExactly(dto);
        verify(cartRepository).findAllById(ids);
        verify(cartEntityMapper).convertToGetCartDto(cart);
    }

    @Test
    @DisplayName("create создает корзину для аккаунта и возвращает DTO")
    void create_savesCartForAccountAndReturnsDto() {
        // Arrange: аккаунт найден, repository сохраняет корзину, mapper возвращает DTO.
        AccountEntity account = accountEntity();
        GetCartDto responseDto = getCartDto();

        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(cartRepository.save(any(CartEntity.class))).thenAnswer(invocation -> {
            CartEntity cart = invocation.getArgument(0);
            cart.setId(CART_ID);
            return cart;
        });
        when(cartEntityMapper.convertToGetCartDto(any(CartEntity.class))).thenReturn(responseDto);

        // Act: создаем корзину через сервис.
        GetCartDto result = cartService.create(new CreateCartDto(ACCOUNT_ID));

        // Assert: корзина создана для нужного аккаунта и преобразована в DTO.
        assertThat(result).isEqualTo(responseDto);

        ArgumentCaptor<CartEntity> cartCaptor = ArgumentCaptor.forClass(CartEntity.class);
        verify(cartRepository).save(cartCaptor.capture());
        assertThat(cartCaptor.getValue().getAccount()).isEqualTo(account);
        verify(cartEntityMapper).convertToGetCartDto(any(CartEntity.class));
    }

    @Test
    @DisplayName("addToCart создает новую позицию корзины")
    void addToCart_whenProductIsNotInCart_createsCartItem() {
        // Arrange: корзина и продукт существуют, в корзине пока нет этого продукта.
        CartEntity cart = cartEntity();
        ProductEntity product = productEntity();
        CreateCartItemDto dto = new CreateCartItemDto(CART_ID, PRODUCT_ID, 3);

        when(productService.getRequiredActiveProduct(PRODUCT_ID)).thenReturn(product);
        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));

        // Act: добавляем товар в корзину.
        cartService.addToCart(dto);

        // Assert: сервис создал cart item с нужными связями и количеством.
        ArgumentCaptor<CartItemEntity> cartItemCaptor = ArgumentCaptor.forClass(CartItemEntity.class);
        verify(cartItemRepository).save(cartItemCaptor.capture());

        CartItemEntity savedCartItem = cartItemCaptor.getValue();
        assertThat(savedCartItem.getCart()).isEqualTo(cart);
        assertThat(savedCartItem.getProduct()).isEqualTo(product);
        assertThat(savedCartItem.getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("addToCart увеличивает количество, если товар уже есть в корзине")
    void addToCart_whenProductAlreadyInCart_increasesQuantity() {
        // Arrange: в корзине уже есть позиция с нужным продуктом.
        CartEntity cart = cartEntity();
        ProductEntity product = productEntity();
        CartItemEntity existingCartItem = cartItemEntity(cart, product, 2);
        cart.getProducts().add(existingCartItem);

        when(productService.getRequiredActiveProduct(PRODUCT_ID)).thenReturn(product);
        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));

        // Act: добавляем тот же товар еще раз.
        cartService.addToCart(new CreateCartItemDto(CART_ID, PRODUCT_ID, 3));

        // Assert: сервис увеличил количество и сохранил существующую позицию.
        assertThat(existingCartItem.getQuantity()).isEqualTo(5);
        verify(cartItemRepository).save(existingCartItem);
    }

    @Test
    @DisplayName("changeQuantity удаляет позицию корзины при quantity = 0")
    void changeQuantity_whenQuantityIsZero_deletesCartItem() {
        // Arrange: позиция корзины найдена, новое количество равно нулю.
        CartItemEntity cartItem = cartItemEntity(cartEntity(), productEntity(), 2);

        when(cartItemRepository.findById(CART_ITEM_ID)).thenReturn(Optional.of(cartItem));

        // Act: меняем количество на 0.
        cartService.changeQuantity(new UpdateCartItemDto(CART_ITEM_ID, 0));

        // Assert: сервис удалил позицию корзины.
        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    @DisplayName("changeQuantity обновляет количество, если quantity больше 0")
    void changeQuantity_whenQuantityIsPositive_updatesCartItem() {
        // Arrange: позиция корзины найдена, mapper обновляет entity.
        CartItemEntity cartItem = cartItemEntity(cartEntity(), productEntity(), 2);
        UpdateCartItemDto dto = new UpdateCartItemDto(CART_ITEM_ID, 5);

        when(cartItemRepository.findById(CART_ITEM_ID)).thenReturn(Optional.of(cartItem));
        when(cartItemEntityMapper.updateWithNull(dto, cartItem)).thenAnswer(invocation -> {
            CartItemEntity entity = invocation.getArgument(1);
            entity.setQuantity(dto.quantity());
            return entity;
        });

        // Act: меняем количество на положительное значение.
        cartService.changeQuantity(dto);

        // Assert: сервис обновил позицию корзины и сохранил ее.
        assertThat(cartItem.getQuantity()).isEqualTo(5);
        verify(cartItemEntityMapper).updateWithNull(dto, cartItem);
        verify(cartItemRepository).save(cartItem);
    }

    @Test
    @DisplayName("removeFromCart удаляет позицию корзины, если она найдена")
    void removeFromCart_whenCartItemExists_deletesCartItem() {
        // Arrange: repository находит позицию корзины.
        CartItemEntity cartItem = cartItemEntity(cartEntity(), productEntity(), 2);

        when(cartItemRepository.findById(CART_ITEM_ID)).thenReturn(Optional.of(cartItem));

        // Act: удаляем позицию корзины через сервис.
        cartService.removeFromCart(CART_ITEM_ID);

        // Assert: сервис нашел entity и передал ее в delete.
        verify(cartItemRepository).findById(CART_ITEM_ID);
        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    @DisplayName("removeManyFromCart удаляет позиции корзины по списку id")
    void removeManyFromCart_deletesByIds() {
        // Arrange: готовим список id для массового удаления.
        List<Long> ids = List.of(10L, 11L);

        // Act: удаляем несколько позиций корзины через сервис.
        cartService.removeManyFromCart(ids);

        // Assert: сервис делегировал удаление репозиторию.
        verify(cartItemRepository).deleteAllById(ids);
    }
}
