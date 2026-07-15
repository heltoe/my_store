package com.example.my_store.courier.service;

import com.example.my_store.courier.controller.dto.CreateUpdateCourierDto;
import com.example.my_store.courier.controller.dto.GetCourierDto;
import com.example.my_store.courier.repository.CourierRepository;
import com.example.my_store.courier.repository.entity.CourierEntity;
import com.example.my_store.courier.utils.CourierEntityMapper;
import com.example.my_store.delivery.repository.DeliveryRepository;
import com.example.my_store.order.repository.order.entity.STATE_ORDER;
import com.example.my_store.utils.exception.CommonEntityNotFoundException;
import com.example.my_store.utils.exception.CommonConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CourierServiceImpl implements CourierService {

    private final CourierEntityMapper courierEntityMapper;

    private final CourierRepository courierRepository;

    private final DeliveryRepository deliveryRepository;

    private CourierEntity getRequiredCourier(Long id) {
        return  courierRepository.findById(id).orElseThrow(() -> new CommonEntityNotFoundException("Courier with id `%s` not found".formatted(id)));
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
        if (deliveryRepository.existsByCourier_IdAndOrder_Status(id, STATE_ORDER.DELIVERY_ON_THE_WAY)) {
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
