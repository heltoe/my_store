package com.example.my_store.payment.service;

import com.example.my_store.order.repository.order.OrderRepository;
import com.example.my_store.order.repository.order.entity.OrderEntity;
import com.example.my_store.order.repository.order.entity.STATE_ORDER;
import com.example.my_store.order.service.OrderService;
import com.example.my_store.payment.controller.dto.CreatePaymentDto;
import com.example.my_store.payment.controller.dto.GetPaymentDto;
import com.example.my_store.payment.repository.PaymentRepository;
import com.example.my_store.payment.repository.entity.PaymentEntity;
import com.example.my_store.payment.repository.entity.STATE_PAYMENT;
import com.example.my_store.payment.utils.PaymentEntityMapper;
import com.example.my_store.utils.exception.CommonConflictException;
import com.example.my_store.utils.exception.CommonEntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    private static final Long PAYMENT_ID = 1L;
    private static final Long ORDER_ID = 2L;

    @Mock
    private PaymentEntityMapper paymentEntityMapper;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private static OrderEntity orderEntity(STATE_ORDER status) {
        OrderEntity order = new OrderEntity();
        order.setId(ORDER_ID);
        order.setStatus(status);
        return order;
    }

    private static PaymentEntity paymentEntity(STATE_PAYMENT status) {
        PaymentEntity payment = new PaymentEntity();
        payment.setId(PAYMENT_ID);
        payment.setOrder(orderEntity(STATE_ORDER.ACCEPTED));
        payment.setStatus(status);
        return payment;
    }

    private static GetPaymentDto getPaymentDto(STATE_PAYMENT status) {
        return new GetPaymentDto(
                new Date(),
                LocalDateTime.now(),
                PAYMENT_ID,
                ORDER_ID,
                status
        );
    }

    private static CreatePaymentDto createPaymentDto() {
        return new CreatePaymentDto(ORDER_ID);
    }

    @Test
    @DisplayName("getAll возвращает страницу платежей, преобразованную в DTO")
    void getAll_returnsMappedPage() {
        PaymentEntity entity = paymentEntity(STATE_PAYMENT.PENDING);
        GetPaymentDto dto = getPaymentDto(STATE_PAYMENT.PENDING);
        Pageable pageable = PageRequest.of(0, 10);

        when(paymentRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity)));
        when(paymentEntityMapper.convertToGetPaymentDto(entity)).thenReturn(dto);

        var result = paymentService.getAll(pageable);

        assertThat(result.getContent()).containsExactly(dto);
        verify(paymentRepository).findAll(pageable);
        verify(paymentEntityMapper).convertToGetPaymentDto(entity);
    }

    @Test
    @DisplayName("getOne возвращает DTO, если платеж найден")
    void getOne_whenPaymentExists_returnsDto() {
        PaymentEntity entity = paymentEntity(STATE_PAYMENT.PENDING);
        GetPaymentDto dto = getPaymentDto(STATE_PAYMENT.PENDING);

        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(entity));
        when(paymentEntityMapper.convertToGetPaymentDto(entity)).thenReturn(dto);

        GetPaymentDto result = paymentService.getOne(PAYMENT_ID);

        assertThat(result).isEqualTo(dto);
        verify(paymentRepository).findById(PAYMENT_ID);
        verify(paymentEntityMapper).convertToGetPaymentDto(entity);
    }

    @Test
    @DisplayName("getOne бросает 404, если платеж не найден")
    void getOne_whenPaymentDoesNotExist_throwsNotFound() {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getOne(PAYMENT_ID))
                .isInstanceOf(CommonEntityNotFoundException.class)
                .hasMessage("Payment with id `1` not found");

        verify(paymentRepository).findById(PAYMENT_ID);
    }

    @Test
    @DisplayName("getByOrderId возвращает DTO, если платеж для заказа найден")
    void getByOrderId_whenPaymentExists_returnsDto() {
        PaymentEntity entity = paymentEntity(STATE_PAYMENT.PENDING);
        GetPaymentDto dto = getPaymentDto(STATE_PAYMENT.PENDING);

        when(paymentRepository.findByOrder_Id(ORDER_ID)).thenReturn(Optional.of(entity));
        when(paymentEntityMapper.convertToGetPaymentDto(entity)).thenReturn(dto);

        GetPaymentDto result = paymentService.getByOrderId(ORDER_ID);

        assertThat(result).isEqualTo(dto);
        verify(paymentRepository).findByOrder_Id(ORDER_ID);
        verify(paymentEntityMapper).convertToGetPaymentDto(entity);
    }

    @Test
    @DisplayName("getByOrderId бросает 404, если платеж для заказа не найден")
    void getByOrderId_whenPaymentDoesNotExist_throwsNotFound() {
        when(paymentRepository.findByOrder_Id(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getByOrderId(ORDER_ID))
                .isInstanceOf(CommonEntityNotFoundException.class)
                .hasMessage("Payment for order with id `2` not found");

        verify(paymentRepository).findByOrder_Id(ORDER_ID);
    }

    @Test
    @DisplayName("getMany возвращает список платежей, преобразованный в DTO")
    void getMany_returnsMappedDtos() {
        PaymentEntity entity = paymentEntity(STATE_PAYMENT.PENDING);
        GetPaymentDto dto = getPaymentDto(STATE_PAYMENT.PENDING);
        List<Long> ids = List.of(PAYMENT_ID);

        when(paymentRepository.findAllById(ids)).thenReturn(List.of(entity));
        when(paymentEntityMapper.convertToGetPaymentDto(entity)).thenReturn(dto);

        List<GetPaymentDto> result = paymentService.getMany(ids);

        assertThat(result).containsExactly(dto);
        verify(paymentRepository).findAllById(ids);
        verify(paymentEntityMapper).convertToGetPaymentDto(entity);
    }

    @Test
    @DisplayName("create создает платеж для заказа в статусе ACCEPTED")
    void create_whenOrderAcceptedAndNoExistingPayment_savesPaymentWithPendingStatus() {
        CreatePaymentDto requestDto = createPaymentDto();
        PaymentEntity entity = paymentEntity(STATE_PAYMENT.PENDING);
        GetPaymentDto responseDto = getPaymentDto(STATE_PAYMENT.PENDING);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(orderEntity(STATE_ORDER.ACCEPTED)));
        when(paymentRepository.findByOrder_Id(ORDER_ID)).thenReturn(Optional.empty());
        when(paymentEntityMapper.convertToEntity(requestDto)).thenReturn(entity);
        when(paymentRepository.save(entity)).thenReturn(entity);
        when(paymentEntityMapper.convertToGetPaymentDto(entity)).thenReturn(responseDto);

        GetPaymentDto result = paymentService.create(requestDto);

        assertThat(result).isEqualTo(responseDto);
        assertThat(entity.getStatus()).isEqualTo(STATE_PAYMENT.PENDING);
        verify(orderRepository).findById(ORDER_ID);
        verify(paymentRepository).findByOrder_Id(ORDER_ID);
        verify(paymentRepository).save(entity);
    }

    @Test
    @DisplayName("create бросает 404, если заказ не найден")
    void create_whenOrderDoesNotExist_throwsNotFound() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.create(createPaymentDto()))
                .isInstanceOf(CommonEntityNotFoundException.class)
                .hasMessage("Order with id `2` not found");
    }

    @Test
    @DisplayName("create бросает 409, если заказ не в статусе ACCEPTED")
    void create_whenOrderStatusIsNotAccepted_throwsConflict() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(orderEntity(STATE_ORDER.CREATED)));

        assertThatThrownBy(() -> paymentService.create(createPaymentDto()))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Order status must be `ACCEPTED`");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("create бросает 409, если для заказа уже есть платеж")
    void create_whenPaymentForOrderAlreadyExists_throwsConflict() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(orderEntity(STATE_ORDER.ACCEPTED)));
        when(paymentRepository.findByOrder_Id(ORDER_ID)).thenReturn(Optional.of(paymentEntity(STATE_PAYMENT.PENDING)));

        assertThatThrownBy(() -> paymentService.create(createPaymentDto()))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Payment for order with id `2` already exists");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("markSuccess переводит платеж в SUCCESS и меняет статус заказа на PAID")
    void markSuccess_whenPaymentIsPending_updatesStatusAndChangesOrderState() {
        PaymentEntity entity = paymentEntity(STATE_PAYMENT.PENDING);
        GetPaymentDto responseDto = getPaymentDto(STATE_PAYMENT.SUCCESS);

        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(entity));
        when(paymentRepository.save(entity)).thenReturn(entity);
        when(paymentEntityMapper.convertToGetPaymentDto(entity)).thenReturn(responseDto);

        GetPaymentDto result = paymentService.markSuccess(PAYMENT_ID);

        assertThat(result).isEqualTo(responseDto);
        assertThat(entity.getStatus()).isEqualTo(STATE_PAYMENT.SUCCESS);
        verify(paymentRepository).save(entity);
        verify(orderService).changeOrderState(ORDER_ID, STATE_ORDER.PAID);
    }

    @Test
    @DisplayName("markSuccess бросает 404, если платеж не найден")
    void markSuccess_whenPaymentDoesNotExist_throwsNotFound() {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.markSuccess(PAYMENT_ID))
                .isInstanceOf(CommonEntityNotFoundException.class)
                .hasMessage("Payment with id `1` not found");

        verify(orderService, never()).changeOrderState(any(), any());
    }

    @Test
    @DisplayName("markSuccess бросает 409, если платеж не в статусе PENDING")
    void markSuccess_whenPaymentIsNotPending_throwsConflict() {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(paymentEntity(STATE_PAYMENT.SUCCESS)));

        assertThatThrownBy(() -> paymentService.markSuccess(PAYMENT_ID))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Payment status must be `PENDING`");

        verify(paymentRepository, never()).save(any());
        verify(orderService, never()).changeOrderState(any(), any());
    }

    @Test
    @DisplayName("markFailure переводит платеж в FAILURE")
    void markFailure_whenPaymentIsPending_updatesStatus() {
        PaymentEntity entity = paymentEntity(STATE_PAYMENT.PENDING);
        GetPaymentDto responseDto = getPaymentDto(STATE_PAYMENT.FAILURE);

        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(entity));
        when(paymentRepository.save(entity)).thenReturn(entity);
        when(paymentEntityMapper.convertToGetPaymentDto(entity)).thenReturn(responseDto);

        GetPaymentDto result = paymentService.markFailure(PAYMENT_ID);

        assertThat(result).isEqualTo(responseDto);
        assertThat(entity.getStatus()).isEqualTo(STATE_PAYMENT.FAILURE);
        verify(paymentRepository).save(entity);
        verify(orderService, never()).changeOrderState(any(), any());
    }

    @Test
    @DisplayName("markFailure бросает 409, если платеж не в статусе PENDING")
    void markFailure_whenPaymentIsNotPending_throwsConflict() {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(paymentEntity(STATE_PAYMENT.FAILURE)));

        assertThatThrownBy(() -> paymentService.markFailure(PAYMENT_ID))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Payment status must be `PENDING`");

        verify(paymentRepository, never()).save(any());
    }
}
