package com.example.service;

import com.example.common_lib.config.ServiceUrlsProperties;
import com.example.common_lib.dto.GetAccountDto;
import com.example.common_lib.dto.GetProductDto;
import com.example.common_lib.utils.exception.CommonEntityNotFoundException;
import com.example.controller.dto.cart.CreateCartDto;
import com.example.controller.dto.cart.GetCartDto;
import com.example.controller.dto.cart_item.CreateCartItemDto;
import com.example.controller.dto.cart_item.UpdateCartItemDto;
import com.example.repository.cart.CartRepository;
import com.example.repository.cart.entity.CartEntity;
import com.example.repository.cart_item.CartItemRepository;
import com.example.repository.cart_item.entity.CartItemEntity;
import com.example.utils.CartEntityMapper;
import com.example.utils.CartItemEntityMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.time.Duration;
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
    private CartItemRepository cartItemRepository;

    private MockWebServer mockWebServer;
    private CartServiceImpl cartService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = mockWebServer.url("/").toString().replaceAll("/$", "");

        ServiceUrlsProperties serviceUrls = new ServiceUrlsProperties();
        serviceUrls.setAccount(baseUrl);
        serviceUrls.setProducts(baseUrl);

        cartService = new CartServiceImpl(
                createWebClient(),
                serviceUrls,
                cartEntityMapper,
                cartItemEntityMapper,
                cartRepository,
                cartItemRepository
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    private static WebClient createWebClient() {
        HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofSeconds(3));
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
    }

    private static CartEntity cartEntity() {
        CartEntity cart = new CartEntity();
        cart.setId(CART_ID);
        cart.setAccount_id(ACCOUNT_ID);
        return cart;
    }

    private static CartItemEntity cartItemEntity(CartEntity cart, Integer quantity) {
        CartItemEntity cartItem = new CartItemEntity();
        cartItem.setId(CART_ITEM_ID);
        cartItem.setCart(cart);
        cartItem.setProductId(PRODUCT_ID);
        cartItem.setQuantity(quantity);
        return cartItem;
    }

    private static GetCartDto getCartDto() {
        return new GetCartDto(new Date(), LocalDateTime.now(), CART_ID);
    }

    private void enqueueAccount() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {
                            "createdAt": "2026-01-01T00:00:00.000+00:00",
                            "updatedAt": "2026-01-01T00:00:00",
                            "id": 2,
                            "phoneNumber": "+79255702395",
                            "firstName": "Владислав",
                            "secondName": "Сергеевич",
                            "lastName": "Жулинский"
                        }""")
                .addHeader("Content-Type", "application/json"));
    }

    private void enqueueProduct(boolean active) {
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {
                            "createdAt": "2026-01-01T00:00:00.000+00:00",
                            "updatedAt": "2026-01-01T00:00:00",
                            "id": 3,
                            "name": "Ноутбук",
                            "description": "Игровой ноутбук",
                            "price": 100000.0,
                            "quantity": 5,
                            "isActive": %s
                        }""".formatted(active))
                .addHeader("Content-Type", "application/json"));
    }

    @Test
    @DisplayName("getAll возвращает страницу корзин, преобразованную в DTO")
    void getAll_returnsMappedPage() {
        CartEntity cart = cartEntity();
        GetCartDto dto = getCartDto();
        Pageable pageable = PageRequest.of(0, 10);

        when(cartRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(cart)));
        when(cartEntityMapper.convertToGetCartDto(cart)).thenReturn(dto);

        var result = cartService.getAll(pageable);

        assertThat(result.getContent()).containsExactly(dto);
        verify(cartRepository).findAll(pageable);
        verify(cartEntityMapper).convertToGetCartDto(cart);
    }

    @Test
    @DisplayName("getOne возвращает DTO, если корзина найдена")
    void getOne_whenCartExists_returnsDto() {
        CartEntity cart = cartEntity();
        GetCartDto dto = getCartDto();

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));
        when(cartEntityMapper.convertToGetCartDto(cart)).thenReturn(dto);

        GetCartDto result = cartService.getOne(CART_ID);

        assertThat(result).isEqualTo(dto);
        verify(cartRepository).findById(CART_ID);
        verify(cartEntityMapper).convertToGetCartDto(cart);
    }

    @Test
    @DisplayName("getOne бросает 404, если корзина не найдена")
    void getOne_whenCartDoesNotExist_throwsNotFound() {
        when(cartRepository.findById(CART_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getOne(CART_ID))
                .isInstanceOf(CommonEntityNotFoundException.class)
                .hasMessage("Cart with id `1` not found");

        verify(cartRepository).findById(CART_ID);
    }

    @Test
    @DisplayName("getMany возвращает список корзин, преобразованный в DTO")
    void getMany_returnsMappedDtos() {
        CartEntity cart = cartEntity();
        GetCartDto dto = getCartDto();
        List<Long> ids = List.of(CART_ID);

        when(cartRepository.findAllById(ids)).thenReturn(List.of(cart));
        when(cartEntityMapper.convertToGetCartDto(cart)).thenReturn(dto);

        List<GetCartDto> result = cartService.getMany(ids);

        assertThat(result).containsExactly(dto);
        verify(cartRepository).findAllById(ids);
        verify(cartEntityMapper).convertToGetCartDto(cart);
    }

    @Test
    @DisplayName("create создает корзину для аккаунта и возвращает DTO")
    void create_savesCartForAccountAndReturnsDto() {
        GetCartDto responseDto = getCartDto();
        enqueueAccount();

        when(cartRepository.save(any(CartEntity.class))).thenAnswer(invocation -> {
            CartEntity cart = invocation.getArgument(0);
            cart.setId(CART_ID);
            return cart;
        });
        when(cartEntityMapper.convertToGetCartDto(any(CartEntity.class))).thenReturn(responseDto);

        GetCartDto result = cartService.create(new CreateCartDto(ACCOUNT_ID));

        assertThat(result).isEqualTo(responseDto);

        ArgumentCaptor<CartEntity> cartCaptor = ArgumentCaptor.forClass(CartEntity.class);
        verify(cartRepository).save(cartCaptor.capture());
        assertThat(cartCaptor.getValue().getAccount_id()).isEqualTo(ACCOUNT_ID);
        verify(cartEntityMapper).convertToGetCartDto(any(CartEntity.class));
    }

    @Test
    @DisplayName("addToCart создает новую позицию корзины")
    void addToCart_whenProductIsNotInCart_createsCartItem() {
        CartEntity cart = cartEntity();
        CreateCartItemDto dto = new CreateCartItemDto(CART_ID, PRODUCT_ID, 3);

        enqueueProduct(true);
        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));

        cartService.addToCart(dto);

        ArgumentCaptor<CartItemEntity> cartItemCaptor = ArgumentCaptor.forClass(CartItemEntity.class);
        verify(cartItemRepository).save(cartItemCaptor.capture());

        CartItemEntity savedCartItem = cartItemCaptor.getValue();
        assertThat(savedCartItem.getCart()).isEqualTo(cart);
        assertThat(savedCartItem.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(savedCartItem.getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("addToCart увеличивает количество, если товар уже есть в корзине")
    void addToCart_whenProductAlreadyInCart_increasesQuantity() {
        CartEntity cart = cartEntity();
        CartItemEntity existingCartItem = cartItemEntity(cart, 2);
        cart.getProducts().add(existingCartItem);

        enqueueProduct(true);
        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));

        cartService.addToCart(new CreateCartItemDto(CART_ID, PRODUCT_ID, 3));

        assertThat(existingCartItem.getQuantity()).isEqualTo(5);
        verify(cartItemRepository).save(existingCartItem);
    }

    @Test
    @DisplayName("changeQuantity удаляет позицию корзины при quantity = 0")
    void changeQuantity_whenQuantityIsZero_deletesCartItem() {
        CartItemEntity cartItem = cartItemEntity(cartEntity(), 2);

        enqueueProduct(true);
        when(cartItemRepository.findById(CART_ITEM_ID)).thenReturn(Optional.of(cartItem));

        cartService.changeQuantity(new UpdateCartItemDto(CART_ITEM_ID, 0));

        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    @DisplayName("changeQuantity обновляет количество, если quantity больше 0")
    void changeQuantity_whenQuantityIsPositive_updatesCartItem() {
        CartItemEntity cartItem = cartItemEntity(cartEntity(), 2);
        UpdateCartItemDto dto = new UpdateCartItemDto(CART_ITEM_ID, 5);

        enqueueProduct(true);
        when(cartItemRepository.findById(CART_ITEM_ID)).thenReturn(Optional.of(cartItem));
        when(cartItemEntityMapper.updateWithNull(dto, cartItem)).thenAnswer(invocation -> {
            CartItemEntity entity = invocation.getArgument(1);
            entity.setQuantity(dto.quantity());
            return entity;
        });

        cartService.changeQuantity(dto);

        assertThat(cartItem.getQuantity()).isEqualTo(5);
        verify(cartItemEntityMapper).updateWithNull(dto, cartItem);
        verify(cartItemRepository).save(cartItem);
    }

    @Test
    @DisplayName("removeFromCart удаляет позицию корзины, если она найдена")
    void removeFromCart_whenCartItemExists_deletesCartItem() {
        CartItemEntity cartItem = cartItemEntity(cartEntity(), 2);

        when(cartItemRepository.findById(CART_ITEM_ID)).thenReturn(Optional.of(cartItem));

        cartService.removeFromCart(CART_ITEM_ID);

        verify(cartItemRepository).findById(CART_ITEM_ID);
        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    @DisplayName("removeManyFromCart удаляет позиции корзины по списку id")
    void removeManyFromCart_deletesByIds() {
        List<Long> ids = List.of(10L, 11L);

        cartService.removeManyFromCart(ids);

        verify(cartItemRepository).deleteAllById(ids);
    }
}
