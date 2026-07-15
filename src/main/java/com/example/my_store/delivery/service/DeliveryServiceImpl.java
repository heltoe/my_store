package com.example.my_store.delivery.service;

import com.example.my_store.delivery.controller.dto.CreateDeliveryDto;
import com.example.my_store.delivery.controller.dto.GetDeliveryDto;
import com.example.my_store.delivery.controller.dto.UpdateDeliveryDto;
import com.example.my_store.delivery.repository.DeliveryRepository;
import com.example.my_store.delivery.repository.entity.DeliveryEntity;
import com.example.my_store.delivery.utils.DeliveryEntityFilter;
import com.example.my_store.delivery.utils.DeliveryEntityMapper;
import com.example.my_store.courier.repository.CourierRepository;
import com.example.my_store.courier.repository.entity.CourierEntity;
import com.example.my_store.order.repository.order.OrderRepository;
import com.example.my_store.order.repository.order.entity.OrderEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.example.my_store.utils.exception.CommonEntityNotFoundException;
import com.example.my_store.utils.exception.CommonConflictException;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryEntityMapper deliveryEntityMapper;

    private final DeliveryRepository deliveryRepository;

    private final CourierRepository courierRepository;

    private final OrderRepository orderRepository;

    private CourierEntity getRequiredCourier(Long id) {
        return courierRepository.findById(id).orElseThrow(() -> new CommonEntityNotFoundException("Courier with id `%s` not found".formatted(id)));
    }

    private DeliveryEntity getRequiredDelivery(Long id) {
        return deliveryRepository.findById(id).orElseThrow(() -> new CommonEntityNotFoundException("Delivery with id `%s` not found".formatted(id)));
    }

    private OrderEntity getRequiredOrder(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new CommonEntityNotFoundException("Order with id `%s` not found".formatted(id)));
    }
    
    @Override
    public Page<GetDeliveryDto> getAll(DeliveryEntityFilter filter, Pageable pageable) {
        Specification<DeliveryEntity> spec = filter.toSpecification();
        Page<DeliveryEntity> deliveryEntities = deliveryRepository.findAll(spec, pageable);
        return deliveryEntities.map(deliveryEntityMapper::convertToGetDeliveryDto);
    }

    @Override
    public GetDeliveryDto getOne(Long id) {
        DeliveryEntity entity = getRequiredDelivery(id);
        return deliveryEntityMapper.convertToGetDeliveryDto(entity);
    }

    @Override
    public List<GetDeliveryDto> getMany(List<Long> ids) {
        List<DeliveryEntity> deliveryEntities = deliveryRepository.findAllById(ids);
        return deliveryEntities.stream()
                .map(deliveryEntityMapper::convertToGetDeliveryDto)
                .toList();
    }

    @Override
    public GetDeliveryDto create(CreateDeliveryDto dto) {
        getRequiredOrder(dto.orderId());
        Optional<DeliveryEntity> findDeliveryByIdOrder = deliveryRepository.findByOrder_Id(dto.orderId());
        if (findDeliveryByIdOrder.isPresent()) {
            throw new CommonConflictException( "Delivery for order with id `%s` already exists".formatted(dto.orderId()));
        }
        CourierEntity courier = getRequiredCourier(dto.courierId());
        if (!courier.getIsActive()) {
            throw new CommonConflictException("Courier is inactive. Please set active courier");
        }

        DeliveryEntity deliveryEntity = deliveryEntityMapper.convertToEntity(dto);
        DeliveryEntity resultDeliveryEntity = deliveryRepository.save(deliveryEntity);
        return deliveryEntityMapper.convertToGetDeliveryDto(resultDeliveryEntity);
    }

    @Override
    public GetDeliveryDto patch(Long id, UpdateDeliveryDto dto) {
        DeliveryEntity entity = getRequiredDelivery(id);
        CourierEntity courier = getRequiredCourier(dto.courierId());
        if (!courier.getIsActive()) {
            throw new CommonConflictException("Courier is inactive. Please set active courier");
        }

        deliveryEntityMapper.updateWithNull(dto, entity);
        DeliveryEntity resultDeliveryEntity = deliveryRepository.save(entity);

        return deliveryEntityMapper.convertToGetDeliveryDto(resultDeliveryEntity);
    }

    @Override
    public void delete(Long id) {
        DeliveryEntity entity = getRequiredDelivery(id);
        deliveryRepository.delete(entity);
    }
}
