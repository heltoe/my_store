package com.example.service;

import com.example.common_lib.config.ServiceUrlsProperties;
import com.example.common_lib.dto.GetDeliveryDto;
import com.example.common_lib.utils.exception.CommonConflictException;
import com.example.common_lib.utils.exception.CommonEntityNotFoundException;
import com.example.controller.dto.CreateDeliveryDto;
import com.example.controller.dto.UpdateDeliveryDto;
import com.example.repository.DeliveryRepository;
import com.example.repository.entity.DeliveryEntity;
import com.example.utils.DeliveryEntityFilter;
import com.example.utils.DeliveryEntityMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
class DeliveryServiceImplTest {

    private static final Long DELIVERY_ID = 1L;
    private static final Long ORDER_ID = 2L;
    private static final Long COURIER_ID = 3L;
    private static final LocalDateTime DELIVERY_DATE = LocalDateTime.now().plusDays(1);

    @Mock
    private DeliveryEntityMapper deliveryEntityMapper;

    @Mock
    private DeliveryRepository deliveryRepository;

    private MockWebServer mockWebServer;
    private DeliveryServiceImpl deliveryService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = mockWebServer.url("/").toString().replaceAll("/$", "");

        ServiceUrlsProperties serviceUrls = new ServiceUrlsProperties();
        serviceUrls.setOrder(baseUrl);
        serviceUrls.setCourier(baseUrl);

        deliveryService = new DeliveryServiceImpl(
                createWebClient(),
                serviceUrls,
                deliveryEntityMapper,
                deliveryRepository
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

    private static DeliveryEntity deliveryEntity() {
        DeliveryEntity delivery = new DeliveryEntity();
        delivery.setId(DELIVERY_ID);
        delivery.setOrderId(ORDER_ID);
        delivery.setCourierId(COURIER_ID);
        delivery.setDeliveryDate(DELIVERY_DATE);
        delivery.setDeliveryPlace("Москва");
        delivery.setDescription("Оставить у двери");
        delivery.setLat(55.75);
        delivery.setLon(37.61);
        return delivery;
    }

    private static GetDeliveryDto getDeliveryDto() {
        return new GetDeliveryDto(
                new Date(),
                LocalDateTime.now(),
                DELIVERY_ID,
                ORDER_ID,
                COURIER_ID,
                DELIVERY_DATE,
                "Москва",
                "Оставить у двери",
                55.75,
                37.61
        );
    }

    private static CreateDeliveryDto createDeliveryDto() {
        return new CreateDeliveryDto(
                ORDER_ID,
                COURIER_ID,
                DELIVERY_DATE,
                "Москва",
                "Оставить у двери",
                55.75,
                37.61
        );
    }

    private static UpdateDeliveryDto updateDeliveryDto() {
        return new UpdateDeliveryDto(
                COURIER_ID,
                DELIVERY_DATE,
                "Москва",
                "Оставить у двери",
                55.75,
                37.61
        );
    }

    private void enqueueOrder() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {
                            "createdAt": "2026-01-01T00:00:00.000+00:00",
                            "updatedAt": "2026-01-01T00:00:00",
                            "id": 2,
                            "stateOrder": "WAIT_BIND_TO_COURIER"
                        }""")
                .addHeader("Content-Type", "application/json"));
    }

    private void enqueueCourier(boolean active) {
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {
                            "createdAt": "2026-01-01T00:00:00.000+00:00",
                            "updatedAt": "2026-01-01T00:00:00",
                            "id": 3,
                            "name": "Владислав",
                            "secondName": "Сергеевич",
                            "lastName": "Жулинский",
                            "phoneNumber": "+79255702395",
                            "isActive": %s
                        }""".formatted(active))
                .addHeader("Content-Type", "application/json"));
    }

    private void enqueueOrderNotFound() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));
    }

    @Test
    @DisplayName("getAll возвращает страницу доставок, преобразованную в DTO")
    void getAll_returnsMappedPage() {
        DeliveryEntity entity = deliveryEntity();
        GetDeliveryDto dto = getDeliveryDto();
        Pageable pageable = PageRequest.of(0, 10);
        DeliveryEntityFilter filter = new DeliveryEntityFilter(DELIVERY_ID, DELIVERY_DATE);

        when(deliveryRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(deliveryEntityMapper.convertToGetDeliveryDto(entity)).thenReturn(dto);

        var result = deliveryService.getAll(filter, pageable);

        assertThat(result.getContent()).containsExactly(dto);
        verify(deliveryRepository).findAll(any(Specification.class), any(Pageable.class));
        verify(deliveryEntityMapper).convertToGetDeliveryDto(entity);
    }

    @Test
    @DisplayName("getOne возвращает DTO, если доставка найдена")
    void getOne_whenDeliveryExists_returnsDto() {
        DeliveryEntity entity = deliveryEntity();
        GetDeliveryDto dto = getDeliveryDto();

        when(deliveryRepository.findById(DELIVERY_ID)).thenReturn(Optional.of(entity));
        when(deliveryEntityMapper.convertToGetDeliveryDto(entity)).thenReturn(dto);

        GetDeliveryDto result = deliveryService.getOne(DELIVERY_ID);

        assertThat(result).isEqualTo(dto);
        verify(deliveryRepository).findById(DELIVERY_ID);
        verify(deliveryEntityMapper).convertToGetDeliveryDto(entity);
    }

    @Test
    @DisplayName("getOne бросает 404, если доставка не найдена")
    void getOne_whenDeliveryDoesNotExist_throwsNotFound() {
        when(deliveryRepository.findById(DELIVERY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryService.getOne(DELIVERY_ID))
                .isInstanceOf(CommonEntityNotFoundException.class)
                .hasMessage("Delivery with id `1` not found");

        verify(deliveryRepository).findById(DELIVERY_ID);
    }

    @Test
    @DisplayName("getMany возвращает список доставок, преобразованный в DTO")
    void getMany_returnsMappedDtos() {
        DeliveryEntity entity = deliveryEntity();
        GetDeliveryDto dto = getDeliveryDto();
        List<Long> ids = List.of(DELIVERY_ID);

        when(deliveryRepository.findAllById(ids)).thenReturn(List.of(entity));
        when(deliveryEntityMapper.convertToGetDeliveryDto(entity)).thenReturn(dto);

        List<GetDeliveryDto> result = deliveryService.getMany(ids);

        assertThat(result).containsExactly(dto);
        verify(deliveryRepository).findAllById(ids);
        verify(deliveryEntityMapper).convertToGetDeliveryDto(entity);
    }

    @Test
    @DisplayName("create создает доставку для существующего заказа и активного курьера")
    void create_whenOrderExistsAndCourierIsActive_savesDelivery() {
        CreateDeliveryDto requestDto = createDeliveryDto();
        DeliveryEntity entity = deliveryEntity();
        GetDeliveryDto responseDto = getDeliveryDto();

        enqueueOrder();
        enqueueCourier(true);
        when(deliveryRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());
        when(deliveryEntityMapper.convertToEntity(requestDto)).thenReturn(entity);
        when(deliveryRepository.save(entity)).thenReturn(entity);
        when(deliveryEntityMapper.convertToGetDeliveryDto(entity)).thenReturn(responseDto);

        GetDeliveryDto result = deliveryService.create(requestDto);

        assertThat(result).isEqualTo(responseDto);
        verify(deliveryRepository).findByOrderId(ORDER_ID);
        verify(deliveryRepository).save(entity);
    }

    @Test
    @DisplayName("create бросает 404, если заказ не найден")
    void create_whenOrderDoesNotExist_throwsNotFound() {
        enqueueOrderNotFound();

        assertThatThrownBy(() -> deliveryService.create(createDeliveryDto()))
                .isInstanceOf(CommonEntityNotFoundException.class)
                .hasMessage("Order with id `2` not found");
    }

    @Test
    @DisplayName("create бросает 409, если для заказа уже есть доставка")
    void create_whenDeliveryForOrderAlreadyExists_throwsConflict() {
        enqueueOrder();
        when(deliveryRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(deliveryEntity()));

        assertThatThrownBy(() -> deliveryService.create(createDeliveryDto()))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Delivery for order with id `2` already exists");
    }

    @Test
    @DisplayName("create бросает 409, если курьер неактивен")
    void create_whenCourierIsInactive_throwsConflict() {
        enqueueOrder();
        enqueueCourier(false);
        when(deliveryRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryService.create(createDeliveryDto()))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Courier is inactive. Please set active courier");
    }

    @Test
    @DisplayName("patch обновляет доставку при активном курьере")
    void patch_whenCourierIsActive_updatesDelivery() {
        DeliveryEntity entity = deliveryEntity();
        UpdateDeliveryDto requestDto = updateDeliveryDto();
        GetDeliveryDto responseDto = getDeliveryDto();

        enqueueCourier(true);
        when(deliveryRepository.findById(DELIVERY_ID)).thenReturn(Optional.of(entity));
        when(deliveryRepository.save(entity)).thenReturn(entity);
        when(deliveryEntityMapper.convertToGetDeliveryDto(entity)).thenReturn(responseDto);

        GetDeliveryDto result = deliveryService.patch(DELIVERY_ID, requestDto);

        assertThat(result).isEqualTo(responseDto);
        verify(deliveryEntityMapper).updateWithNull(requestDto, entity);
        verify(deliveryRepository).save(entity);
    }

    @Test
    @DisplayName("patch бросает 409, если курьер неактивен")
    void patch_whenCourierIsInactive_throwsConflict() {
        enqueueCourier(false);
        when(deliveryRepository.findById(DELIVERY_ID)).thenReturn(Optional.of(deliveryEntity()));

        assertThatThrownBy(() -> deliveryService.patch(DELIVERY_ID, updateDeliveryDto()))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Courier is inactive. Please set active courier");
    }

    @Test
    @DisplayName("delete удаляет доставку, если она найдена")
    void delete_whenDeliveryExists_deletesEntity() {
        DeliveryEntity entity = deliveryEntity();
        when(deliveryRepository.findById(DELIVERY_ID)).thenReturn(Optional.of(entity));

        deliveryService.delete(DELIVERY_ID);

        verify(deliveryRepository).findById(DELIVERY_ID);
        verify(deliveryRepository).delete(entity);
    }
}
