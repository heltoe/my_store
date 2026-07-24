package com.example.service;

import com.example.common_lib.dto.GetCourierDto;
import com.example.common_lib.dto.GetDeliveryDto;
import com.example.common_lib.dto.GetOrderDto;
import com.example.common_lib.dto.STATE_ORDER;
import com.example.controller.dto.CreateUpdateCourierDto;
import com.example.repository.CourierRepository;
import com.example.repository.entity.CourierEntity;
import com.example.utils.CourierEntityMapper;
import com.example.common_lib.utils.exception.CommonConflictException;
import com.example.common_lib.utils.exception.CommonEntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CourierServiceImpl implements CourierService {
    private final WebClient webClient;

    private final CourierEntityMapper courierEntityMapper;

    private final CourierRepository courierRepository;

    private CourierEntity getRequiredCourier(Long id) {
        return  courierRepository.findById(id).orElseThrow(() -> new CommonEntityNotFoundException("Courier with id `%s` not found".formatted(id)));
    }

    public GetDeliveryDto getRequiredDelivery(Long id) {
        return webClient
                .get()
                .uri(":8080/rest/deliveries/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.error(new CommonEntityNotFoundException("Delivery with id `%s` not found".formatted(id))))
                .bodyToMono(GetDeliveryDto.class)
                .block();
    }

    public GetOrderDto getRequiredOrder(Long id) {
        return webClient
                .get()
                .uri(":8080/rest/orders/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.error(new CommonEntityNotFoundException("Order with id `%s` not found".formatted(id))))
                .bodyToMono(GetOrderDto.class)
                .block();
    }

    private void throwIfPhoneNumberExists(String phoneNumber) {
        if (courierRepository.existsByPhoneNumber(phoneNumber)) {
            throw new CommonConflictException("Entity with phone number `%s` already exists".formatted(phoneNumber));
        }
    }

    private void throwIfPhoneNumberExistsForAnotherCourier(String phoneNumber, Long id) {
        if (courierRepository.existsByPhoneNumberAndIdNot(phoneNumber, id)) {
            throw new CommonConflictException("Entity with phone number `%s` already exists".formatted(phoneNumber));
        }
    }

    private void throwIfCourierHasDeliveryOnTheWay(Long id) {
        GetDeliveryDto entityDelivery = getRequiredDelivery(id);
        GetOrderDto entityOrder = getRequiredOrder(entityDelivery.orderId());
        if (entityOrder.stateOrder().equals(STATE_ORDER.DELIVERY_ON_THE_WAY)) {
            throw new CommonConflictException("Courier has delivery on the way");
        }
    }

    @Override
    public Page<GetCourierDto> getAll(Pageable pageable) {
        Page<CourierEntity> courierEntities = courierRepository.findAll(pageable);
        return courierEntities.map(courierEntityMapper::convertToGetCourierDto);
    }

    @Override
    public GetCourierDto getOne(Long id) {
        CourierEntity entity = getRequiredCourier(id);
        return courierEntityMapper.convertToGetCourierDto(entity);
    }

    @Override
    public List<GetCourierDto> getMany(List<Long> ids) {
        List<CourierEntity> courierEntities = courierRepository.findAllById(ids);
        return courierEntities.stream()
                .map(courierEntityMapper::convertToGetCourierDto)
                .toList();
    }

    @Override
    public GetCourierDto create(CreateUpdateCourierDto dto) {
        throwIfPhoneNumberExists(dto.phoneNumber());
        CourierEntity courierEntity = courierEntityMapper.convertToEntity(dto);
        courierEntity.setIsActive(true);
        CourierEntity resultCourierEntity = courierRepository.save(courierEntity);
        return courierEntityMapper.convertToGetCourierDto(resultCourierEntity);
    }

    @Override
    public GetCourierDto patch(Long id, CreateUpdateCourierDto dto) {
        CourierEntity entity = getRequiredCourier(id);

        throwIfPhoneNumberExistsForAnotherCourier(dto.phoneNumber(), id);
        courierEntityMapper.updateWithNull(dto, entity);

        CourierEntity resultCourierEntity = courierRepository.save(entity);
        return courierEntityMapper.convertToGetCourierDto(resultCourierEntity);
    }

    @Override
    public void setInactiveCourier(Long id) {
        CourierEntity courier = getRequiredCourier(id);
        throwIfCourierHasDeliveryOnTheWay(id);
        courier.setIsActive(false);
        courierRepository.save(courier);
    }

    @Override
    public void setActiveCourier(Long id) {
        CourierEntity courier = getRequiredCourier(id);
        courier.setIsActive(true);
        courierRepository.save(courier);
    }
}
