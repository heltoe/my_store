package com.example.service;

import com.example.common_lib.config.ServiceUrlsProperties;
import com.example.common_lib.dto.GetOrderDto;
import com.example.common_lib.dto.STATE_ORDER;
import com.example.common_lib.utils.exception.CommonConflictException;
import com.example.common_lib.utils.exception.CommonEntityNotFoundException;
import com.example.controller.dto.BaseCreateOrderDto;
import com.example.controller.dto.CreateOrderDto;
import com.example.controller.dto.CreateOrderItemDto;
import com.example.repository.order.OrderRepository;
import com.example.repository.order.entity.OrderEntity;
import com.example.repository.order_item.entity.OrderItemEntity;
import com.example.utils.OrderEntityMapper;
import com.example.utils.OrderItemEntityMapper;
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
class OrderServiceImplTest {

    private static final Long ORDER_ID = 1L;
    private static final Long ACCOUNT_ID = 2L;
    private static final Long PRODUCT_ID = 3L;

    @Mock
    private OrderEntityMapper orderEntityMapper;

    @Mock
    private OrderItemEntityMapper orderItemEntityMapper;

    @Mock
    private OrderRepository orderRepository;

    private MockWebServer mockWebServer;
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = mockWebServer.url("/").toString().replaceAll("/$", "");

        ServiceUrlsProperties serviceUrls = new ServiceUrlsProperties();
        serviceUrls.setAccount(baseUrl);
        serviceUrls.setProducts(baseUrl);

        orderService = new OrderServiceImpl(
                createWebClient(),
                serviceUrls,
                orderEntityMapper,
                orderItemEntityMapper,
                orderRepository
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

    private static OrderEntity orderEntity(STATE_ORDER status) {
        OrderEntity order = new OrderEntity();
        order.setId(ORDER_ID);
        order.setAccountId(ACCOUNT_ID);
        order.setStatus(status);
        return order;
    }

    private static OrderItemEntity orderItemEntity() {
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setQuantity(2);
        return orderItem;
    }

    private static GetOrderDto getOrderDto() {
        return new GetOrderDto(new Date(), LocalDateTime.now(), ORDER_ID, STATE_ORDER.CREATED);
    }

    private static CreateOrderDto createOrderDto() {
        return new CreateOrderDto(
                new BaseCreateOrderDto(ACCOUNT_ID),
                List.of(new CreateOrderItemDto(PRODUCT_ID, 2))
        );
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

    private void enqueueProduct() {
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
                            "isActive": true
                        }""")
                .addHeader("Content-Type", "application/json"));
    }

    private void enqueueAccountNotFound() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));
    }

    private void enqueueProductNotFound() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));
    }

    @Test
    @DisplayName("getAll возвращает страницу заказов, преобразованную в DTO")
    void getAll_returnsMappedPage() {
        OrderEntity order = orderEntity(STATE_ORDER.CREATED);
        GetOrderDto dto = getOrderDto();
        Pageable pageable = PageRequest.of(0, 10);

        when(orderRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(order)));
        when(orderEntityMapper.convertToGetOrderDto(order)).thenReturn(dto);

        var result = orderService.getAll(pageable);

        assertThat(result.getContent()).containsExactly(dto);
        verify(orderRepository).findAll(pageable);
        verify(orderEntityMapper).convertToGetOrderDto(order);
    }

    @Test
    @DisplayName("getOne возвращает DTO, если заказ найден")
    void getOne_whenOrderExists_returnsDto() {
        OrderEntity order = orderEntity(STATE_ORDER.CREATED);
        GetOrderDto dto = getOrderDto();

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderEntityMapper.convertToGetOrderDto(order)).thenReturn(dto);

        GetOrderDto result = orderService.getOne(ORDER_ID);

        assertThat(result).isEqualTo(dto);
        verify(orderRepository).findById(ORDER_ID);
        verify(orderEntityMapper).convertToGetOrderDto(order);
    }

    @Test
    @DisplayName("getOne бросает 404, если заказ не найден")
    void getOne_whenOrderDoesNotExist_throwsNotFound() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOne(ORDER_ID))
                .isInstanceOf(CommonEntityNotFoundException.class)
                .hasMessage("Order with id `1` not found");

        verify(orderRepository).findById(ORDER_ID);
    }

    @Test
    @DisplayName("getMany возвращает список заказов, преобразованный в DTO")
    void getMany_returnsMappedDtos() {
        OrderEntity order = orderEntity(STATE_ORDER.CREATED);
        GetOrderDto dto = getOrderDto();
        List<Long> ids = List.of(ORDER_ID);

        when(orderRepository.findAllById(ids)).thenReturn(List.of(order));
        when(orderEntityMapper.convertToGetOrderDto(order)).thenReturn(dto);

        List<GetOrderDto> result = orderService.getMany(ids);

        assertThat(result).containsExactly(dto);
        verify(orderRepository).findAllById(ids);
        verify(orderEntityMapper).convertToGetOrderDto(order);
    }

    @Test
    @DisplayName("create создает заказ с позицией и берет цену из продукта")
    void create_savesOrderWithProductPrice() {
        OrderEntity order = new OrderEntity();
        OrderItemEntity orderItem = orderItemEntity();
        CreateOrderDto requestDto = createOrderDto();
        GetOrderDto responseDto = getOrderDto();

        enqueueAccount();
        enqueueProduct();
        when(orderEntityMapper.convertToEntity(requestDto.orderDto())).thenReturn(order);
        when(orderItemEntityMapper.convertToEntity(requestDto.items().get(0))).thenReturn(orderItem);
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> {
            OrderEntity savedOrder = invocation.getArgument(0);
            savedOrder.setId(ORDER_ID);
            return savedOrder;
        });
        when(orderEntityMapper.convertToGetOrderDto(any(OrderEntity.class))).thenReturn(responseDto);

        GetOrderDto result = orderService.create(requestDto);

        assertThat(result).isEqualTo(responseDto);

        ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(orderCaptor.capture());

        OrderEntity savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getAccountId()).isEqualTo(ACCOUNT_ID);
        assertThat(savedOrder.getStatus()).isEqualTo(STATE_ORDER.CREATED);
        assertThat(savedOrder.getProducts()).containsExactly(orderItem);
        assertThat(orderItem.getOrder()).isEqualTo(savedOrder);
        assertThat(orderItem.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(orderItem.getPrice()).isEqualTo(100_000.0);
        assertThat(orderItem.getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("create бросает 404, если аккаунт не найден")
    void create_whenAccountDoesNotExist_throwsNotFound() {
        enqueueAccountNotFound();

        assertThatThrownBy(() -> orderService.create(createOrderDto()))
                .isInstanceOf(CommonEntityNotFoundException.class)
                .hasMessage("Account with id `2` not found");
    }

    @Test
    @DisplayName("create бросает 404, если продукт не найден")
    void create_whenProductDoesNotExist_throwsNotFound() {
        enqueueAccount();
        when(orderEntityMapper.convertToEntity(any(BaseCreateOrderDto.class))).thenReturn(new OrderEntity());
        enqueueProductNotFound();

        assertThatThrownBy(() -> orderService.create(createOrderDto()))
                .isInstanceOf(CommonEntityNotFoundException.class)
                .hasMessage("Product with id `3` not found");
    }

    @Test
    @DisplayName("changeOrderState переводит заказ из CREATED в ACCEPTED")
    void changeOrderState_whenCreatedToAccepted_updatesStatus() {
        OrderEntity order = orderEntity(STATE_ORDER.CREATED);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        orderService.changeOrderState(ORDER_ID, STATE_ORDER.ACCEPTED);

        assertThat(order.getStatus()).isEqualTo(STATE_ORDER.ACCEPTED);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("changeOrderState бросает 409 при некорректном переходе")
    void changeOrderState_whenStatusTransitionIsInvalid_throwsConflict() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(orderEntity(STATE_ORDER.CREATED)));

        assertThatThrownBy(() -> orderService.changeOrderState(ORDER_ID, STATE_ORDER.PAID))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Order status must be `ACCEPTED`");
    }

    @Test
    @DisplayName("changeOrderState отменяет заказ из любого не финального статуса")
    void changeOrderState_whenStatusIsNotReceivedOrCanceled_cancelsOrder() {
        OrderEntity order = orderEntity(STATE_ORDER.PAID);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        orderService.changeOrderState(ORDER_ID, STATE_ORDER.CANCELED);

        assertThat(order.getStatus()).isEqualTo(STATE_ORDER.CANCELED);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("changeOrderState запрещает отмену полученного заказа")
    void changeOrderState_whenOrderIsReceived_throwsConflictOnCancel() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(orderEntity(STATE_ORDER.RECEIVED_BY_USER)));

        assertThatThrownBy(() -> orderService.changeOrderState(ORDER_ID, STATE_ORDER.CANCELED))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Order with status `RECEIVED_BY_USER` cannot be canceled");
    }

    @Test
    @DisplayName("changeOrderState запрещает повторную отмену заказа")
    void changeOrderState_whenOrderIsCanceled_throwsConflictOnCancel() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(orderEntity(STATE_ORDER.CANCELED)));

        assertThatThrownBy(() -> orderService.changeOrderState(ORDER_ID, STATE_ORDER.CANCELED))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Order with status `CANCELED` cannot be canceled");
    }
}
