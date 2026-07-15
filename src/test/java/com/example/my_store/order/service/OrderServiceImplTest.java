package com.example.my_store.order.service;

import com.example.my_store.account.repository.AccountRepository;
import com.example.my_store.account.repository.entity.AccountEntity;
import com.example.my_store.order.controller.dto.BaseCreateOrderDto;
import com.example.my_store.order.controller.dto.CreateOrderDto;
import com.example.my_store.order.controller.dto.CreateOrderItemDto;
import com.example.my_store.order.controller.dto.GetOrderDto;
import com.example.my_store.order.repository.order.OrderRepository;
import com.example.my_store.order.repository.order.entity.OrderEntity;
import com.example.my_store.order.repository.order.entity.STATE_ORDER;
import com.example.my_store.order.repository.order_item.entity.OrderItemEntity;
import com.example.my_store.order.utils.OrderEntityMapper;
import com.example.my_store.order.utils.OrderItemEntityMapper;
import com.example.my_store.product.repository.entity.ProductEntity;
import com.example.my_store.product.service.ProductService;
import com.example.my_store.utils.exception.CommonConflictException;
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

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderServiceImpl orderService;

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

    private static OrderEntity orderEntity(STATE_ORDER status) {
        OrderEntity order = new OrderEntity();
        order.setId(ORDER_ID);
        order.setAccount(accountEntity());
        order.setStatus(status);
        return order;
    }

    private static OrderItemEntity orderItemEntity() {
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setQuantity(2);
        return orderItem;
    }

    private static GetOrderDto getOrderDto() {
        return new GetOrderDto(new Date(), LocalDateTime.now(), ORDER_ID);
    }

    private static CreateOrderDto createOrderDto() {
        return new CreateOrderDto(
                new BaseCreateOrderDto(ACCOUNT_ID),
                List.of(new CreateOrderItemDto(PRODUCT_ID, 2))
        );
    }

    @Test
    @DisplayName("getAll возвращает страницу заказов, преобразованную в DTO")
    void getAll_returnsMappedPage() {
        // Arrange: repository возвращает страницу entity, mapper превращает entity в DTO.
        OrderEntity order = orderEntity(STATE_ORDER.CREATED);
        GetOrderDto dto = getOrderDto();
        Pageable pageable = PageRequest.of(0, 10);

        when(orderRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(order)));
        when(orderEntityMapper.convertToGetOrderDto(order)).thenReturn(dto);

        // Act: запрашиваем страницу заказов через сервис.
        var result = orderService.getAll(pageable);

        // Assert: получили ожидаемую страницу DTO и проверили вызовы зависимостей.
        assertThat(result.getContent()).containsExactly(dto);
        verify(orderRepository).findAll(pageable);
        verify(orderEntityMapper).convertToGetOrderDto(order);
    }

    @Test
    @DisplayName("getOne возвращает DTO, если заказ найден")
    void getOne_whenOrderExists_returnsDto() {
        // Arrange: repository находит заказ, mapper превращает entity в DTO.
        OrderEntity order = orderEntity(STATE_ORDER.CREATED);
        GetOrderDto dto = getOrderDto();

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderEntityMapper.convertToGetOrderDto(order)).thenReturn(dto);

        // Act: запрашиваем заказ по id.
        GetOrderDto result = orderService.getOne(ORDER_ID);

        // Assert: сервис вернул DTO и обратился к нужным зависимостям.
        assertThat(result).isEqualTo(dto);
        verify(orderRepository).findById(ORDER_ID);
        verify(orderEntityMapper).convertToGetOrderDto(order);
    }

    @Test
    @DisplayName("getOne бросает 404, если заказ не найден")
    void getOne_whenOrderDoesNotExist_throwsNotFound() {
        // Arrange: repository не находит заказ.
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        // Act + Assert: вызов сервиса должен завершиться ошибкой ненайденной entity.
        assertThatThrownBy(() -> orderService.getOne(ORDER_ID))
                .isInstanceOf(CommonEntityNotFoundException.class)
                .hasMessage("Order with id `1` not found");

        verify(orderRepository).findById(ORDER_ID);
    }

    @Test
    @DisplayName("getMany возвращает список заказов, преобразованный в DTO")
    void getMany_returnsMappedDtos() {
        // Arrange: repository возвращает список entity, mapper превращает их в DTO.
        OrderEntity order = orderEntity(STATE_ORDER.CREATED);
        GetOrderDto dto = getOrderDto();
        List<Long> ids = List.of(ORDER_ID);

        when(orderRepository.findAllById(ids)).thenReturn(List.of(order));
        when(orderEntityMapper.convertToGetOrderDto(order)).thenReturn(dto);

        // Act: запрашиваем несколько заказов по id.
        List<GetOrderDto> result = orderService.getMany(ids);

        // Assert: получили ожидаемый список DTO и проверили вызовы.
        assertThat(result).containsExactly(dto);
        verify(orderRepository).findAllById(ids);
        verify(orderEntityMapper).convertToGetOrderDto(order);
    }

    @Test
    @DisplayName("create создает заказ с позицией и берет цену из продукта")
    void create_savesOrderWithProductPrice() {
        // Arrange: аккаунт и продукт существуют, mapper создает order и item entity.
        AccountEntity account = accountEntity();
        ProductEntity product = productEntity();
        OrderEntity order = new OrderEntity();
        OrderItemEntity orderItem = orderItemEntity();
        CreateOrderDto requestDto = createOrderDto();
        GetOrderDto responseDto = getOrderDto();

        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(productService.getRequiredActiveProduct(PRODUCT_ID)).thenReturn(product);
        when(orderEntityMapper.convertToEntity(requestDto.orderDto())).thenReturn(order);
        when(orderItemEntityMapper.convertToEntity(requestDto.items().get(0))).thenReturn(orderItem);
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> {
            OrderEntity savedOrder = invocation.getArgument(0);
            savedOrder.setId(ORDER_ID);
            return savedOrder;
        });
        when(orderEntityMapper.convertToGetOrderDto(any(OrderEntity.class))).thenReturn(responseDto);

        // Act: создаем заказ через сервис.
        GetOrderDto result = orderService.create(requestDto);

        // Assert: заказ сохранен с нужными связями, статусом и ценой из ProductEntity.
        assertThat(result).isEqualTo(responseDto);

        ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(orderCaptor.capture());

        OrderEntity savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getAccount()).isEqualTo(account);
        assertThat(savedOrder.getStatus()).isEqualTo(STATE_ORDER.CREATED);
        assertThat(savedOrder.getItems()).containsExactly(orderItem);
        assertThat(orderItem.getOrder()).isEqualTo(savedOrder);
        assertThat(orderItem.getProduct()).isEqualTo(product);
        assertThat(orderItem.getPrice()).isEqualTo(product.getPrice());
        assertThat(orderItem.getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("create бросает 404, если аккаунт не найден")
    void create_whenAccountDoesNotExist_throwsNotFound() {
        // Arrange: repository не находит аккаунт.
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        // Act + Assert: создание заказа должно завершиться ошибкой ненайденного аккаунта.
        assertThatThrownBy(() -> orderService.create(createOrderDto()))
                .isInstanceOf(CommonEntityNotFoundException.class)
                .hasMessage("Account with id `2` not found");

        verify(accountRepository).findById(ACCOUNT_ID);
    }

    @Test
    @DisplayName("create бросает 404, если продукт не найден")
    void create_whenProductDoesNotExist_throwsNotFound() {
        // Arrange: аккаунт найден, но продукт отсутствует.
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity()));
        when(orderEntityMapper.convertToEntity(any(BaseCreateOrderDto.class))).thenReturn(new OrderEntity());
        when(productService.getRequiredActiveProduct(PRODUCT_ID))
                .thenThrow(new CommonEntityNotFoundException("Product with id `%s` not found".formatted(PRODUCT_ID)));

        // Act + Assert: создание заказа должно завершиться ошибкой ненайденного продукта.
        assertThatThrownBy(() -> orderService.create(createOrderDto()))
                .isInstanceOf(CommonEntityNotFoundException.class)
                .hasMessage("Product with id `3` not found");

        verify(productService).getRequiredActiveProduct(PRODUCT_ID);
    }

    @Test
    @DisplayName("changeOrderState переводит заказ из CREATED в ACCEPTED")
    void changeOrderState_whenCreatedToAccepted_updatesStatus() {
        // Arrange: заказ найден в статусе CREATED.
        OrderEntity order = orderEntity(STATE_ORDER.CREATED);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        // Act: меняем статус заказа.
        orderService.changeOrderState(ORDER_ID, STATE_ORDER.ACCEPTED);

        // Assert: статус обновлен и заказ сохранен.
        assertThat(order.getStatus()).isEqualTo(STATE_ORDER.ACCEPTED);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("changeOrderState бросает 409 при некорректном переходе")
    void changeOrderState_whenStatusTransitionIsInvalid_throwsConflict() {
        // Arrange: заказ еще CREATED, а его пытаются сразу оплатить.
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(orderEntity(STATE_ORDER.CREATED)));

        // Act + Assert: переход CREATED -> PAID должен завершиться conflict-ошибкой.
        assertThatThrownBy(() -> orderService.changeOrderState(ORDER_ID, STATE_ORDER.PAID))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Order status must be `ACCEPTED`");
    }

    @Test
    @DisplayName("changeOrderState отменяет заказ из любого не финального статуса")
    void changeOrderState_whenStatusIsNotReceivedOrCanceled_cancelsOrder() {
        // Arrange: заказ находится в статусе PAID.
        OrderEntity order = orderEntity(STATE_ORDER.PAID);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        // Act: отменяем заказ.
        orderService.changeOrderState(ORDER_ID, STATE_ORDER.CANCELED);

        // Assert: заказ переведен в статус CANCELED и сохранен.
        assertThat(order.getStatus()).isEqualTo(STATE_ORDER.CANCELED);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("changeOrderState запрещает отмену полученного заказа")
    void changeOrderState_whenOrderIsReceived_throwsConflictOnCancel() {
        // Arrange: заказ уже получен пользователем.
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(orderEntity(STATE_ORDER.RECEIVED_BY_USER)));

        // Act + Assert: отмена полученного заказа запрещена.
        assertThatThrownBy(() -> orderService.changeOrderState(ORDER_ID, STATE_ORDER.CANCELED))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Order with status `RECEIVED_BY_USER` cannot be canceled");
    }

    @Test
    @DisplayName("changeOrderState запрещает повторную отмену заказа")
    void changeOrderState_whenOrderIsCanceled_throwsConflictOnCancel() {
        // Arrange: заказ уже отменен.
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(orderEntity(STATE_ORDER.CANCELED)));

        // Act + Assert: повторная отмена запрещена текущей бизнес-логикой.
        assertThatThrownBy(() -> orderService.changeOrderState(ORDER_ID, STATE_ORDER.CANCELED))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Order with status `CANCELED` cannot be canceled");
    }
}
