package com.example.my_store.courier.service;

import com.example.my_store.courier.controller.dto.CreateUpdateCourierDto;
import com.example.my_store.courier.controller.dto.GetCourierDto;
import com.example.my_store.courier.repository.CourierRepository;
import com.example.my_store.courier.repository.entity.CourierEntity;
import com.example.my_store.courier.utils.CourierEntityMapper;
import com.example.my_store.delivery.repository.DeliveryRepository;
import com.example.my_store.order.repository.order.entity.STATE_ORDER;
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

    @Mock
    private DeliveryRepository deliveryRepository;

    @InjectMocks
    private CourierServiceImpl courierService;

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

    @Test
    @DisplayName("getAll возвращает страницу курьеров, преобразованную в DTO")
    void getAll_returnsMappedPage() {
        // Arrange: repository возвращает страницу entity, mapper превращает entity в DTO.
        CourierEntity entity = courierEntity();
        GetCourierDto dto = getCourierDto();
        Pageable pageable = PageRequest.of(0, 10);

        when(courierRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity)));
        when(courierEntityMapper.convertToGetCourierDto(entity)).thenReturn(dto);

        // Act: запрашиваем страницу курьеров через сервис.
        var result = courierService.getAll(pageable);

        // Assert: получили ожидаемую страницу DTO и проверили вызовы зависимостей.
        assertThat(result.getContent()).containsExactly(dto);
        verify(courierRepository).findAll(pageable);
        verify(courierEntityMapper).convertToGetCourierDto(entity);
    }

    @Test
    @DisplayName("getOne возвращает DTO, если курьер найден")
    void getOne_whenCourierExists_returnsDto() {
        // Arrange: repository находит курьера, mapper превращает entity в DTO.
        CourierEntity entity = courierEntity();
        GetCourierDto dto = getCourierDto();

        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(entity));
        when(courierEntityMapper.convertToGetCourierDto(entity)).thenReturn(dto);

        // Act: запрашиваем курьера по id.
        GetCourierDto result = courierService.getOne(COURIER_ID);

        // Assert: сервис вернул DTO и обратился к нужным зависимостям.
        assertThat(result).isEqualTo(dto);
        verify(courierRepository).findById(COURIER_ID);
        verify(courierEntityMapper).convertToGetCourierDto(entity);
    }

    @Test
    @DisplayName("getOne бросает 404, если курьер не найден")
    void getOne_whenCourierDoesNotExist_throwsNotFound() {
        // Arrange: repository не находит курьера.
        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.empty());

        // Act + Assert: вызов сервиса должен завершиться ошибкой ненайденной entity.
        assertThatThrownBy(() -> courierService.getOne(COURIER_ID))
                .isInstanceOf(CommonEntityNotFoundException.class)
                .hasMessage("Courier with id `1` not found");

        // Assert: сервис действительно пытался найти курьера по id.
        verify(courierRepository).findById(COURIER_ID);
    }

    @Test
    @DisplayName("getMany возвращает список курьеров, преобразованный в DTO")
    void getMany_returnsMappedDtos() {
        // Arrange: repository возвращает список entity, mapper превращает их в DTO.
        CourierEntity entity = courierEntity();
        GetCourierDto dto = getCourierDto();
        List<Long> ids = List.of(COURIER_ID);

        when(courierRepository.findAllById(ids)).thenReturn(List.of(entity));
        when(courierEntityMapper.convertToGetCourierDto(entity)).thenReturn(dto);

        // Act: запрашиваем несколько курьеров по id.
        List<GetCourierDto> result = courierService.getMany(ids);

        // Assert: получили ожидаемый список DTO и проверили вызовы.
        assertThat(result).containsExactly(dto);
        verify(courierRepository).findAllById(ids);
        verify(courierEntityMapper).convertToGetCourierDto(entity);
    }

    @Test
    @DisplayName("create сохраняет активного курьера и возвращает DTO")
    void create_savesActiveCourierAndReturnsDto() {
        // Arrange: телефон свободен, mapper создает entity, repository сохраняет ее.
        CreateUpdateCourierDto requestDto = createUpdateDto(PHONE_NUMBER);
        CourierEntity entity = courierEntity();
        entity.setIsActive(null);
        GetCourierDto responseDto = getCourierDto();

        when(courierRepository.existsByPhoneNumber(PHONE_NUMBER)).thenReturn(false);
        when(courierEntityMapper.convertToEntity(requestDto)).thenReturn(entity);
        when(courierRepository.save(entity)).thenReturn(entity);
        when(courierEntityMapper.convertToGetCourierDto(entity)).thenReturn(responseDto);

        // Act: создаем курьера через сервис.
        GetCourierDto result = courierService.create(requestDto);

        // Assert: сервис активировал курьера перед сохранением и вернул DTO.
        assertThat(result).isEqualTo(responseDto);
        assertThat(entity.getIsActive()).isTrue();
        verify(courierRepository).existsByPhoneNumber(PHONE_NUMBER);
        verify(courierRepository).save(entity);
        verify(courierEntityMapper).convertToGetCourierDto(entity);
    }

    @Test
    @DisplayName("create бросает 409, если телефон уже занят")
    void create_whenPhoneNumberExists_throwsConflict() {
        // Arrange: repository сообщает, что телефон уже существует.
        when(courierRepository.existsByPhoneNumber(PHONE_NUMBER)).thenReturn(true);

        // Act + Assert: создание курьера должно завершиться conflict-ошибкой.
        assertThatThrownBy(() -> courierService.create(createUpdateDto(PHONE_NUMBER)))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Entity with phone number `+79255702395` already exists");

        // Assert: сервис проверил уникальность телефона.
        verify(courierRepository).existsByPhoneNumber(PHONE_NUMBER);
    }

    @Test
    @DisplayName("patch обновляет курьера, если телефон не занят другим курьером")
    void patch_whenPhoneNumberIsAvailable_updatesCourier() {
        // Arrange: курьер найден, новый телефон не принадлежит другому курьеру.
        CourierEntity entity = courierEntity();
        CreateUpdateCourierDto requestDto = createUpdateDto(ANOTHER_PHONE_NUMBER);
        GetCourierDto responseDto = getCourierDto();

        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(entity));
        when(courierRepository.existsByPhoneNumberAndIdNot(ANOTHER_PHONE_NUMBER, COURIER_ID)).thenReturn(false);
        when(courierRepository.save(entity)).thenReturn(entity);
        when(courierEntityMapper.convertToGetCourierDto(entity)).thenReturn(responseDto);

        // Act: обновляем курьера через сервис.
        GetCourierDto result = courierService.patch(COURIER_ID, requestDto);

        // Assert: сервис проверил уникальность телефона, обновил entity и вернул DTO.
        assertThat(result).isEqualTo(responseDto);
        verify(courierRepository).findById(COURIER_ID);
        verify(courierRepository).existsByPhoneNumberAndIdNot(ANOTHER_PHONE_NUMBER, COURIER_ID);
        verify(courierEntityMapper).updateWithNull(requestDto, entity);
        verify(courierRepository).save(entity);
    }

    @Test
    @DisplayName("patch бросает 409, если телефон занят другим курьером")
    void patch_whenPhoneNumberBelongsToAnotherCourier_throwsConflict() {
        // Arrange: курьер найден, но новый телефон уже принадлежит другой записи.
        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(courierEntity()));
        when(courierRepository.existsByPhoneNumberAndIdNot(ANOTHER_PHONE_NUMBER, COURIER_ID)).thenReturn(true);

        // Act + Assert: обновление должно завершиться conflict-ошибкой.
        assertThatThrownBy(() -> courierService.patch(COURIER_ID, createUpdateDto(ANOTHER_PHONE_NUMBER)))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Entity with phone number `+79161234567` already exists");

        // Assert: сервис проверил уникальность телефона относительно текущего id.
        verify(courierRepository).existsByPhoneNumberAndIdNot(ANOTHER_PHONE_NUMBER, COURIER_ID);
    }

    @Test
    @DisplayName("setInactiveCourier деактивирует курьера без доставки в пути")
    void setInactiveCourier_whenCourierHasNoDeliveryOnTheWay_setsInactive() {
        // Arrange: курьер найден, доставок в пути нет.
        CourierEntity entity = courierEntity();
        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(entity));
        when(deliveryRepository.existsByCourier_IdAndOrder_Status(COURIER_ID, STATE_ORDER.DELIVERY_ON_THE_WAY))
                .thenReturn(false);

        // Act: деактивируем курьера.
        courierService.setInactiveCourier(COURIER_ID);

        // Assert: сервис изменил флаг активности и сохранил курьера.
        assertThat(entity.getIsActive()).isFalse();
        verify(courierRepository).save(entity);
    }

    @Test
    @DisplayName("setInactiveCourier бросает 409, если у курьера есть доставка в пути")
    void setInactiveCourier_whenCourierHasDeliveryOnTheWay_throwsConflict() {
        // Arrange: курьер найден, есть delivery со статусом DELIVERY_ON_THE_WAY.
        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(courierEntity()));
        when(deliveryRepository.existsByCourier_IdAndOrder_Status(COURIER_ID, STATE_ORDER.DELIVERY_ON_THE_WAY))
                .thenReturn(true);

        // Act + Assert: деактивация должна завершиться conflict-ошибкой.
        assertThatThrownBy(() -> courierService.setInactiveCourier(COURIER_ID))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Courier has delivery on the way");
    }

    @Test
    @DisplayName("setActiveCourier активирует найденного курьера")
    void setActiveCourier_setsActive() {
        // Arrange: repository находит неактивного курьера.
        CourierEntity entity = courierEntity();
        entity.setIsActive(false);
        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(entity));

        // Act: активируем курьера.
        courierService.setActiveCourier(COURIER_ID);

        // Assert: сервис изменил флаг активности и сохранил курьера.
        assertThat(entity.getIsActive()).isTrue();
        verify(courierRepository).save(entity);
    }
}
