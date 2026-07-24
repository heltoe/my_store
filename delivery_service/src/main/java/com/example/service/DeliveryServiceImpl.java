package com.example.service;

import com.example.common_lib.dto.GetCourierDto;
import com.example.common_lib.dto.GetOrderDto;
import com.example.common_lib.dto.GetDeliveryDto;
import com.example.controller.dto.CreateDeliveryDto;
import com.example.controller.dto.UpdateDeliveryDto;
import com.example.repository.DeliveryRepository;
import com.example.repository.entity.DeliveryEntity;
import com.example.utils.DeliveryEntityFilter;
import com.example.utils.DeliveryEntityMapper;
import com.example.common_lib.utils.exception.CommonConflictException;
import com.example.common_lib.utils.exception.CommonEntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class DeliveryServiceImpl implements DeliveryService {
    private final WebClient webClient;

    private final DeliveryEntityMapper deliveryEntityMapper;

    private final DeliveryRepository deliveryRepository;

    public GetOrderDto getRequiredOrder(Long id) {
        return webClient
                .get()
                .uri(":8080/rest/orders/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.error(new CommonEntityNotFoundException("Order with id `%s` not found".formatted(id))))
                .bodyToMono(GetOrderDto.class)
                .block();
    }

    public GetCourierDto getRequiredCourier(Long id) {
        return webClient
                .get()
                .uri(":8080/rest/couriers/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.error(new CommonEntityNotFoundException("Courier with id `%s` not found".formatted(id))))
                .bodyToMono(GetCourierDto.class)
                .block();
    }

    private DeliveryEntity getRequiredDelivery(Long id) {
        return deliveryRepository.findById(id).orElseThrow(() -> new CommonEntityNotFoundException("Delivery with id `%s` not found".formatted(id)));
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
        GetCourierDto courier = getRequiredCourier(dto.courierId());
        if (!courier.isActive()) {
            throw new CommonConflictException("Courier is inactive. Please set active courier");
        }

        DeliveryEntity deliveryEntity = deliveryEntityMapper.convertToEntity(dto);
        DeliveryEntity resultDeliveryEntity = deliveryRepository.save(deliveryEntity);
        return deliveryEntityMapper.convertToGetDeliveryDto(resultDeliveryEntity);
    }

    @Override
    public GetDeliveryDto patch(Long id, UpdateDeliveryDto dto) {
        DeliveryEntity entity = getRequiredDelivery(id);
        GetCourierDto courier = getRequiredCourier(dto.courierId());
        if (!courier.isActive()) {
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
