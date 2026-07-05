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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryEntityMapper deliveryEntityMapper;

    private final DeliveryRepository deliveryRepository;

    private final CourierRepository courierRepository;

    private DeliveryEntity _getOneDelivery(Long id) {
        return deliveryRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery with id `%s` not found".formatted(id)));
    }

    private CourierEntity _getOneCourier(Long id) {
        return courierRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Courier with id `%s` not found".formatted(id)));
    }

    @Override
    public Page<GetDeliveryDto> getAll(DeliveryEntityFilter filter, Pageable pageable) {
        Specification<DeliveryEntity> spec = filter.toSpecification();
        Page<DeliveryEntity> deliveryEntities = deliveryRepository.findAll(spec, pageable);
        return deliveryEntities.map(deliveryEntityMapper::convertToGetDeliveryDto);
    }

    @Override
    public GetDeliveryDto getOne(Long id) {
        DeliveryEntity entity = _getOneDelivery(id);
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
        Optional<DeliveryEntity> findDeliveryByIdOrder = deliveryRepository.findByOrder_Id(dto.orderId());
        if (findDeliveryByIdOrder.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery with id `%s` is existed".formatted(dto.orderId()));
        }
        _getOneCourier(dto.courierId());

        DeliveryEntity deliveryEntity = deliveryEntityMapper.convertToEntity(dto);
        DeliveryEntity resultDeliveryEntity = deliveryRepository.save(deliveryEntity);
        return deliveryEntityMapper.convertToGetDeliveryDto(resultDeliveryEntity);
    }

    @Override
    public GetDeliveryDto patch(Long id, UpdateDeliveryDto dto) {
        DeliveryEntity entity = _getOneDelivery(id);
        _getOneCourier(dto.courierId());

        deliveryEntityMapper.updateWithNull(dto, entity);
        DeliveryEntity resultDeliveryEntity = deliveryRepository.save(entity);

        return deliveryEntityMapper.convertToGetDeliveryDto(resultDeliveryEntity);
    }

    @Override
    public void delete(Long id) {
        DeliveryEntity entity = _getOneDelivery(id);
        if (entity != null) {
            deliveryRepository.delete(entity);
        }
    }

    @Override
    public void deleteMany(List<Long> ids) {
        deliveryRepository.deleteAllById(ids);
    }
}
