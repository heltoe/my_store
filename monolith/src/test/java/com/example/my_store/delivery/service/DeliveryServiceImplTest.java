package com.example.my_store.delivery.service;

import com.example.my_store.courier.repository.CourierRepository;
import com.example.my_store.courier.repository.entity.CourierEntity;
import com.example.my_store.delivery.controller.dto.CreateDeliveryDto;
import com.example.my_store.delivery.controller.dto.GetDeliveryDto;
import com.example.my_store.delivery.controller.dto.UpdateDeliveryDto;
import com.example.my_store.delivery.repository.DeliveryRepository;
import com.example.my_store.delivery.repository.entity.DeliveryEntity;
import com.example.my_store.delivery.utils.DeliveryEntityFilter;
import com.example.my_store.delivery.utils.DeliveryEntityMapper;
import com.example.my_store.order.repository.order.OrderRepository;
import com.example.my_store.order.repository.order.entity.OrderEntity;
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
import org.springframework.data.jpa.domain.Specification;

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

    @Mock
    private CourierRepository courierRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private DeliveryServiceImpl deliveryService;

    private static OrderEntity orderEntity() {
        OrderEntity order = new OrderEntity();
        order.setId(ORDER_ID);
        order.setStatus(STATE_ORDER.WAIT_BIND_TO_COURIER);
        return order;
    }

    private static CourierEntity courierEntity(Boolean isActive) {
        CourierEntity courier = new CourierEntity();
        courier.setId(COURIER_ID);
        courier.setName("Владислав");
        courier.setLastName("Жулинский");
        courier.setPhoneNumber("+79255702395");
        courier.setIsActive(isActive);
        return courier;
    }

    private static DeliveryEntity deliveryEntity() {
        DeliveryEntity delivery = new DeliveryEntity();
        delivery.setId(DELIVERY_ID);
        delivery.setOrder(orderEntity());
        delivery.setCourier(courierEntity(true));
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

    @Test
    @DisplayName("getAll возвращает страницу доставок, преобразованную в DTO")
    void getAll_returnsMappedPage() {
        // Arrange: repository возвращает страницу entity, mapper превращает entity в DTO.
        DeliveryEntity entity = deliveryEntity();
        GetDeliveryDto dto = getDeliveryDto();
        Pageable pageable = PageRequest.of(0, 10);
        DeliveryEntityFilter filter = new DeliveryEntityFilter(DELIVERY_ID, DELIVERY_DATE);

        when(deliveryRepository.findAll(anySpecification(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(deliveryEntityMapper.convertToGetDeliveryDto(entity)).thenReturn(dto);

        // Act: запрашиваем страницу доставок через сервис.
        var result = deliveryService.getAll(filter, pageable);

        // Assert: получили ожидаемую страницу DTO и проверили вызовы зависимостей.
        assertThat(result.getContent()).containsExactly(dto);
        verify(deliveryRepository).findAll(anySpecification(), any(Pageable.class));
        verify(deliveryEntityMapper).convertToGetDeliveryDto(entity);
    }

    private static Specification<DeliveryEntity> anySpecification() {
        return any();
    }

    @Test
    @DisplayName("getOne возвращает DTO, если доставка найдена")
    void getOne_whenDeliveryExists_returnsDto() {
        // Arrange: repository находит доставку, mapper превращает entity в DTO.
        DeliveryEntity entity = deliveryEntity();
        GetDeliveryDto dto = getDeliveryDto();

        when(deliveryRepository.findById(DELIVERY_ID)).thenReturn(Optional.of(entity));
        when(deliveryEntityMapper.convertToGetDeliveryDto(entity)).thenReturn(dto);

        // Act: запрашиваем доставку по id.
        GetDeliveryDto result = deliveryService.getOne(DELIVERY_ID);

        // Assert: сервис вернул DTO и обратился к нужным зависимостям.
        assertThat(result).isEqualTo(dto);
        verify(deliveryRepository).findById(DELIVERY_ID);
        verify(deliveryEntityMapper).convertToGetDeliveryDto(entity);
    }

    @Test
    @DisplayName("getOne бросает 404, если доставка не найдена")
    void getOne_whenDeliveryDoesNotExist_throwsNotFound() {
        // Arrange: repository не находит доставку.
        when(deliveryRepository.findById(DELIVERY_ID)).thenReturn(Optional.empty());

        // Act + Assert: вызов сервиса должен завершиться ошибкой ненайденной entity.
        assertThatThrownBy(() -> deliveryService.getOne(DELIVERY_ID))
                .isInstanceOf(CommonEntityNotFoundException.class)
                .hasMessage("Delivery with id `1` not found");

        // Assert: сервис действительно пытался найти доставку по id.
        verify(deliveryRepository).findById(DELIVERY_ID);
    }

    @Test
    @DisplayName("getMany возвращает список доставок, преобразованный в DTO")
    void getMany_returnsMappedDtos() {
        // Arrange: repository возвращает список entity, mapper превращает их в DTO.
        DeliveryEntity entity = deliveryEntity();
        GetDeliveryDto dto = getDeliveryDto();
        List<Long> ids = List.of(DELIVERY_ID);

        when(deliveryRepository.findAllById(ids)).thenReturn(List.of(entity));
        when(deliveryEntityMapper.convertToGetDeliveryDto(entity)).thenReturn(dto);

        // Act: запрашиваем несколько доставок по id.
        List<GetDeliveryDto> result = deliveryService.getMany(ids);

        // Assert: получили ожидаемый список DTO и проверили вызовы.
        assertThat(result).containsExactly(dto);
        verify(deliveryRepository).findAllById(ids);
        verify(deliveryEntityMapper).convertToGetDeliveryDto(entity);
    }

    @Test
    @DisplayName("create создает доставку для существующего заказа и активного курьера")
    void create_whenOrderExistsAndCourierIsActive_savesDelivery() {
        // Arrange: заказ существует, delivery для заказа еще нет, курьер активен.
        CreateDeliveryDto requestDto = createDeliveryDto();
        DeliveryEntity entity = deliveryEntity();
        GetDeliveryDto responseDto = getDeliveryDto();

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(orderEntity()));
        when(deliveryRepository.findByOrder_Id(ORDER_ID)).thenReturn(Optional.empty());
        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(courierEntity(true)));
        when(deliveryEntityMapper.convertToEntity(requestDto)).thenReturn(entity);
        when(deliveryRepository.save(entity)).thenReturn(entity);
        when(deliveryEntityMapper.convertToGetDeliveryDto(entity)).thenReturn(responseDto);

        // Act: создаем доставку через сервис.
        GetDeliveryDto result = deliveryService.create(requestDto);

        // Assert: сервис проверил связи, сохранил delivery и вернул DTO.
        assertThat(result).isEqualTo(responseDto);
        verify(orderRepository).findById(ORDER_ID);
        verify(deliveryRepository).findByOrder_Id(ORDER_ID);
        verify(courierRepository).findById(COURIER_ID);
        verify(deliveryRepository).save(entity);
    }

    @Test
    @DisplayName("create бросает 404, если заказ не найден")
    void create_whenOrderDoesNotExist_throwsNotFound() {
        // Arrange: repository не находит заказ.
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        // Act + Assert: создание доставки должно завершиться ошибкой ненайденного заказа.
        assertThatThrownBy(() -> deliveryService.create(createDeliveryDto()))
                .isInstanceOf(CommonEntityNotFoundException.class)
                .hasMessage("Order with id `2` not found");
    }

    @Test
    @DisplayName("create бросает 409, если для заказа уже есть доставка")
    void create_whenDeliveryForOrderAlreadyExists_throwsConflict() {
        // Arrange: заказ существует, и для него уже есть delivery.
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(orderEntity()));
        when(deliveryRepository.findByOrder_Id(ORDER_ID)).thenReturn(Optional.of(deliveryEntity()));

        // Act + Assert: создание второй доставки для заказа должно завершиться conflict-ошибкой.
        assertThatThrownBy(() -> deliveryService.create(createDeliveryDto()))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Delivery for order with id `2` already exists");
    }

    @Test
    @DisplayName("create бросает 409, если курьер неактивен")
    void create_whenCourierIsInactive_throwsConflict() {
        // Arrange: заказ существует, delivery еще нет, но курьер неактивен.
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(orderEntity()));
        when(deliveryRepository.findByOrder_Id(ORDER_ID)).thenReturn(Optional.empty());
        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(courierEntity(false)));

        // Act + Assert: создание доставки с неактивным курьером должно завершиться conflict-ошибкой.
        assertThatThrownBy(() -> deliveryService.create(createDeliveryDto()))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Courier is inactive. Please set active courier");
    }

    @Test
    @DisplayName("patch обновляет доставку при активном курьере")
    void patch_whenCourierIsActive_updatesDelivery() {
        // Arrange: доставка найдена, новый курьер активен.
        DeliveryEntity entity = deliveryEntity();
        UpdateDeliveryDto requestDto = updateDeliveryDto();
        GetDeliveryDto responseDto = getDeliveryDto();

        when(deliveryRepository.findById(DELIVERY_ID)).thenReturn(Optional.of(entity));
        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(courierEntity(true)));
        when(deliveryRepository.save(entity)).thenReturn(entity);
        when(deliveryEntityMapper.convertToGetDeliveryDto(entity)).thenReturn(responseDto);

        // Act: обновляем доставку через сервис.
        GetDeliveryDto result = deliveryService.patch(DELIVERY_ID, requestDto);

        // Assert: сервис проверил курьера, обновил entity через mapper и вернул DTO.
        assertThat(result).isEqualTo(responseDto);
        verify(courierRepository).findById(COURIER_ID);
        verify(deliveryEntityMapper).updateWithNull(requestDto, entity);
        verify(deliveryRepository).save(entity);
    }

    @Test
    @DisplayName("patch бросает 409, если курьер неактивен")
    void patch_whenCourierIsInactive_throwsConflict() {
        // Arrange: доставка найдена, но выбранный курьер неактивен.
        when(deliveryRepository.findById(DELIVERY_ID)).thenReturn(Optional.of(deliveryEntity()));
        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(courierEntity(false)));

        // Act + Assert: обновление доставки должно завершиться conflict-ошибкой.
        assertThatThrownBy(() -> deliveryService.patch(DELIVERY_ID, updateDeliveryDto()))
                .isInstanceOf(CommonConflictException.class)
                .hasMessage("Courier is inactive. Please set active courier");
    }

    @Test
    @DisplayName("delete удаляет доставку, если она найдена")
    void delete_whenDeliveryExists_deletesEntity() {
        // Arrange: repository находит доставку для удаления.
        DeliveryEntity entity = deliveryEntity();
        when(deliveryRepository.findById(DELIVERY_ID)).thenReturn(Optional.of(entity));

        // Act: удаляем доставку через сервис.
        deliveryService.delete(DELIVERY_ID);

        // Assert: сервис нашел entity и передал ее в delete.
        verify(deliveryRepository).findById(DELIVERY_ID);
        verify(deliveryRepository).delete(entity);
    }
}
