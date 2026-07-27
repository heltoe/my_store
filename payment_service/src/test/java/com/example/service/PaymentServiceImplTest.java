package com.example.service;

import com.example.common_lib.config.ServiceUrlsProperties;
import com.example.common_lib.dto.GetOrderDto;
import com.example.common_lib.dto.STATE_ORDER;
import com.example.common_lib.utils.exception.CommonConflictException;
import com.example.common_lib.utils.exception.CommonEntityNotFoundException;
import com.example.controller.dto.CreatePaymentDto;
import com.example.controller.dto.GetPaymentDto;
import com.example.repository.PaymentRepository;
import com.example.repository.entity.PaymentEntity;
import com.example.repository.entity.STATE_PAYMENT;
import com.example.utils.PaymentEntityMapper;
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
import org.springframework.context.ApplicationEventPublisher;
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
    private ApplicationEventPublisher applicationEventPublisher;

    private MockWebServer mockWebServer;
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = mockWebServer.url("/").toString().replaceAll("/$", "");

        ServiceUrlsProperties serviceUrls = new ServiceUrlsProperties();
        serviceUrls.setOrder(baseUrl);

        paymentService = new PaymentServiceImpl(
                createWebClient(),
                serviceUrls,
                paymentEntityMapper,
                paymentRepository,
                applicationEventPublisher
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

    private static PaymentEntity paymentEntity(STATE_PAYMENT status) {
        PaymentEntity payment = new PaymentEntity();
        payment.setId(PAYMENT_ID);
        payment.setOrderId(ORDER_ID);
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

    private void enqueueOrder(STATE_ORDER status) {
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {
                            "createdAt": "2026-01-01T00:00:00.000+00:00",
                            "updatedAt": "2026-01-01T00:00:00",
                            "id": 2,
                            "stateOrder": "%s"
                        }""".formatted(status))
                .addHeader("Content-Type", "application/json"));
    }

    private void enqueueOrderNotFound() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));
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

        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(entity));
        when(paymentEntityMapper.convertToGetPaymentDto(entity)).thenReturn(dto);

        GetPaymentDto result = paymentService.getByOrderId(ORDER_ID);

        assertThat(result).isEqualTo(dto);
        verify(paymentRepository).findByOrderId(ORDER_ID);
        verify(paymentEntityMapper).convertToGetPaymentDto(entity);
    }

    @Test
    @DisplayName("getByOrderId бросает 404, если платеж для заказа не найден")
    void getByOrderId_whenPaymentDoesNotExist_throwsNotFound() {
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getByOrderId(ORDER_ID))
                .isInstanceOf(CommonEntityNotFoundException.class)
                .hasMessage("Payment for order with id `2` not found");

        verify(paymentRepository).findByOrderId(ORDER_ID);
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

        enqueueOrder(STATE_ORDER.ACCEPTED);
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());
        when(paymentEntityMapper.convertToEntity(requestDto)).thenReturn(entity);
        when(paymentRepository.save(entity)).thenReturn(entity);
        when(paymentEntityMapper.convertToGetPaymentDto(entity)).thenReturn(responseDto);

        GetPaymentDto result = paymentService.create(requestDto);

        assertThat(result).isEqualTo(responseDto);
        assertThat(entity.getStatus()).isEqualTo(STATE_PAYMENT.PENDING);
        verify(paymentRepository).findByOrderId(ORDER_ID);
        verify(paymentRepository).save(entity);
    }

    @Test
    @DisplayName("create бросает 404, если заказ не найден")
    void create_whenOrderDoesNotExist_throwsNotFound() {
        enqueueOrderNotFound();

        assertThatThrownBy(() -> paymentService.create(createPaymentDto()))
                .isInstanceOf(CommonEntityNotFoundException.class)
                .hasMessage("Order with id `2` not found");
    }

    @Test
    @DisplayName("create бросает 409, если заказ не в статусе ACCEPTED")
    void create_whenOrderStatusIsNotAccepted_throwsConflict() {
        enqueueOrder(STATE_ORDER.CREATED);

        assertThatThrownBy(() -> paymentService.create(createPaymentDto()))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Order status must be `ACCEPTED`");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("create бросает 409, если для заказа уже есть платеж")
    void create_whenPaymentForOrderAlreadyExists_throwsConflict() {
        enqueueOrder(STATE_ORDER.ACCEPTED);
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(paymentEntity(STATE_PAYMENT.PENDING)));

        assertThatThrownBy(() -> paymentService.create(createPaymentDto()))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Payment for order with id `2` already exists");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("markSuccess переводит платеж в SUCCESS и публикует событие")
    void markSuccess_whenPaymentIsPending_updatesStatusAndPublishesEvent() {
        PaymentEntity entity = paymentEntity(STATE_PAYMENT.PENDING);
        GetPaymentDto responseDto = getPaymentDto(STATE_PAYMENT.SUCCESS);

        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(entity));
        when(paymentRepository.save(entity)).thenReturn(entity);
        when(paymentEntityMapper.convertToGetPaymentDto(entity)).thenReturn(responseDto);

        GetPaymentDto result = paymentService.markSuccess(PAYMENT_ID);

        assertThat(result).isEqualTo(responseDto);
        assertThat(entity.getStatus()).isEqualTo(STATE_PAYMENT.SUCCESS);
        verify(paymentRepository).save(entity);

        ArgumentCaptor<PaymentMarkedSuccessEvent> eventCaptor = ArgumentCaptor.forClass(PaymentMarkedSuccessEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().payment()).isEqualTo(entity);
    }

    @Test
    @DisplayName("markSuccess бросает 404, если платеж не найден")
    void markSuccess_whenPaymentDoesNotExist_throwsNotFound() {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.markSuccess(PAYMENT_ID))
                .isInstanceOf(CommonEntityNotFoundException.class)
                .hasMessage("Payment with id `1` not found");

        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("markSuccess бросает 409, если платеж не в статусе PENDING")
    void markSuccess_whenPaymentIsNotPending_throwsConflict() {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(paymentEntity(STATE_PAYMENT.SUCCESS)));

        assertThatThrownBy(() -> paymentService.markSuccess(PAYMENT_ID))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Payment status must be `PENDING`");

        verify(paymentRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
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
        verify(applicationEventPublisher, never()).publishEvent(any());
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
