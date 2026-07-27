package com.example.service;

import com.example.common_lib.config.ServiceUrlsProperties;
import com.example.common_lib.dto.GetCourierDto;
import com.example.common_lib.utils.exception.CommonConflictException;
import com.example.common_lib.utils.exception.CommonEntityNotFoundException;
import com.example.controller.dto.CreateUpdateCourierDto;
import com.example.repository.CourierRepository;
import com.example.repository.entity.CourierEntity;
import com.example.utils.CourierEntityMapper;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourierServiceImplTest {

    private static final Long COURIER_ID = 1L;
    private static final String PHONE_NUMBER = "+79255702395";
    private static final String ANOTHER_PHONE_NUMBER = "+79161234567";

    @Mock
    private CourierEntityMapper courierEntityMapper;

    @Mock
    private CourierRepository courierRepository;

    private MockWebServer mockWebServer;
    private CourierServiceImpl courierService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = mockWebServer.url("/").toString().replaceAll("/$", "");

        ServiceUrlsProperties serviceUrls = new ServiceUrlsProperties();
        serviceUrls.setDelivery(baseUrl);

        courierService = new CourierServiceImpl(
                createWebClient(),
                serviceUrls,
                courierEntityMapper,
                courierRepository
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

    private static CourierEntity courierEntity() {
        CourierEntity entity = new CourierEntity();
        entity.setId(COURIER_ID);
        entity.setName("Владислав");
        entity.setSecondName("Сергеевич");
        entity.setLastName("Жулинский");
        entity.setPhoneNumber(PHONE_NUMBER);
        entity.setIsActive(true);
        return entity;
    }

    private static GetCourierDto getCourierDto() {
        return new GetCourierDto(
                new Date(),
                LocalDateTime.now(),
                COURIER_ID,
                "Владислав",
                "Сергеевич",
                "Жулинский",
                PHONE_NUMBER,
                true
        );
    }

    private static CreateUpdateCourierDto createUpdateDto(String phoneNumber) {
        return new CreateUpdateCourierDto(
                "Владислав",
                "Сергеевич",
                "Жулинский",
                phoneNumber
        );
    }

    private void enqueueOnTheWay(boolean onTheWay) {
        mockWebServer.enqueue(new MockResponse()
                .setBody(String.valueOf(onTheWay))
                .addHeader("Content-Type", "application/json"));
    }

    @Test
    @DisplayName("getAll возвращает страницу курьеров, преобразованную в DTO")
    void getAll_returnsMappedPage() {
        CourierEntity entity = courierEntity();
        GetCourierDto dto = getCourierDto();
        Pageable pageable = PageRequest.of(0, 10);

        when(courierRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity)));
        when(courierEntityMapper.convertToGetCourierDto(entity)).thenReturn(dto);

        var result = courierService.getAll(pageable);

        assertThat(result.getContent()).containsExactly(dto);
        verify(courierRepository).findAll(pageable);
        verify(courierEntityMapper).convertToGetCourierDto(entity);
    }

    @Test
    @DisplayName("getOne возвращает DTO, если курьер найден")
    void getOne_whenCourierExists_returnsDto() {
        CourierEntity entity = courierEntity();
        GetCourierDto dto = getCourierDto();

        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(entity));
        when(courierEntityMapper.convertToGetCourierDto(entity)).thenReturn(dto);

        GetCourierDto result = courierService.getOne(COURIER_ID);

        assertThat(result).isEqualTo(dto);
        verify(courierRepository).findById(COURIER_ID);
        verify(courierEntityMapper).convertToGetCourierDto(entity);
    }

    @Test
    @DisplayName("getOne бросает 404, если курьер не найден")
    void getOne_whenCourierDoesNotExist_throwsNotFound() {
        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courierService.getOne(COURIER_ID))
                .isInstanceOf(CommonEntityNotFoundException.class)
                .hasMessage("Courier with id `1` not found");

        verify(courierRepository).findById(COURIER_ID);
    }

    @Test
    @DisplayName("getMany возвращает список курьеров, преобразованный в DTO")
    void getMany_returnsMappedDtos() {
        CourierEntity entity = courierEntity();
        GetCourierDto dto = getCourierDto();
        List<Long> ids = List.of(COURIER_ID);

        when(courierRepository.findAllById(ids)).thenReturn(List.of(entity));
        when(courierEntityMapper.convertToGetCourierDto(entity)).thenReturn(dto);

        List<GetCourierDto> result = courierService.getMany(ids);

        assertThat(result).containsExactly(dto);
        verify(courierRepository).findAllById(ids);
        verify(courierEntityMapper).convertToGetCourierDto(entity);
    }

    @Test
    @DisplayName("create сохраняет активного курьера и возвращает DTO")
    void create_savesActiveCourierAndReturnsDto() {
        CreateUpdateCourierDto requestDto = createUpdateDto(PHONE_NUMBER);
        CourierEntity entity = courierEntity();
        entity.setIsActive(null);
        GetCourierDto responseDto = getCourierDto();

        when(courierRepository.existsByPhoneNumber(PHONE_NUMBER)).thenReturn(false);
        when(courierEntityMapper.convertToEntity(requestDto)).thenReturn(entity);
        when(courierRepository.save(entity)).thenReturn(entity);
        when(courierEntityMapper.convertToGetCourierDto(entity)).thenReturn(responseDto);

        GetCourierDto result = courierService.create(requestDto);

        assertThat(result).isEqualTo(responseDto);
        assertThat(entity.getIsActive()).isTrue();
        verify(courierRepository).existsByPhoneNumber(PHONE_NUMBER);
        verify(courierRepository).save(entity);
        verify(courierEntityMapper).convertToGetCourierDto(entity);
    }

    @Test
    @DisplayName("create бросает 409, если телефон уже занят")
    void create_whenPhoneNumberExists_throwsConflict() {
        when(courierRepository.existsByPhoneNumber(PHONE_NUMBER)).thenReturn(true);

        assertThatThrownBy(() -> courierService.create(createUpdateDto(PHONE_NUMBER)))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Entity with phone number `+79255702395` already exists");

        verify(courierRepository).existsByPhoneNumber(PHONE_NUMBER);
    }

    @Test
    @DisplayName("patch обновляет курьера, если телефон не занят другим курьером")
    void patch_whenPhoneNumberIsAvailable_updatesCourier() {
        CourierEntity entity = courierEntity();
        CreateUpdateCourierDto requestDto = createUpdateDto(ANOTHER_PHONE_NUMBER);
        GetCourierDto responseDto = getCourierDto();

        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(entity));
        when(courierRepository.existsByPhoneNumberAndIdNot(ANOTHER_PHONE_NUMBER, COURIER_ID)).thenReturn(false);
        when(courierRepository.save(entity)).thenReturn(entity);
        when(courierEntityMapper.convertToGetCourierDto(entity)).thenReturn(responseDto);

        GetCourierDto result = courierService.patch(COURIER_ID, requestDto);

        assertThat(result).isEqualTo(responseDto);
        verify(courierRepository).findById(COURIER_ID);
        verify(courierRepository).existsByPhoneNumberAndIdNot(ANOTHER_PHONE_NUMBER, COURIER_ID);
        verify(courierEntityMapper).updateWithNull(requestDto, entity);
        verify(courierRepository).save(entity);
    }

    @Test
    @DisplayName("patch бросает 409, если телефон занят другим курьером")
    void patch_whenPhoneNumberBelongsToAnotherCourier_throwsConflict() {
        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(courierEntity()));
        when(courierRepository.existsByPhoneNumberAndIdNot(ANOTHER_PHONE_NUMBER, COURIER_ID)).thenReturn(true);

        assertThatThrownBy(() -> courierService.patch(COURIER_ID, createUpdateDto(ANOTHER_PHONE_NUMBER)))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Entity with phone number `+79161234567` already exists");

        verify(courierRepository).existsByPhoneNumberAndIdNot(ANOTHER_PHONE_NUMBER, COURIER_ID);
    }

    @Test
    @DisplayName("setInactiveCourier деактивирует курьера без доставки в пути")
    void setInactiveCourier_whenCourierHasNoDeliveryOnTheWay_setsInactive() {
        CourierEntity entity = courierEntity();
        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(entity));
        enqueueOnTheWay(false);

        courierService.setInactiveCourier(COURIER_ID);

        assertThat(entity.getIsActive()).isFalse();
        verify(courierRepository).save(entity);
    }

    @Test
    @DisplayName("setInactiveCourier бросает 409, если у курьера есть доставка в пути")
    void setInactiveCourier_whenCourierHasDeliveryOnTheWay_throwsConflict() {
        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(courierEntity()));
        enqueueOnTheWay(true);

        assertThatThrownBy(() -> courierService.setInactiveCourier(COURIER_ID))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Courier has delivery on the way");
    }

    @Test
    @DisplayName("setActiveCourier активирует найденного курьера")
    void setActiveCourier_setsActive() {
        CourierEntity entity = courierEntity();
        entity.setIsActive(false);
        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(entity));

        courierService.setActiveCourier(COURIER_ID);

        assertThat(entity.getIsActive()).isTrue();
        verify(courierRepository).save(entity);
    }
}
