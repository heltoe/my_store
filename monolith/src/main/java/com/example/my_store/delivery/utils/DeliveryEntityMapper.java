package com.example.my_store.delivery.utils;

import com.example.my_store.courier.repository.entity.CourierEntity;
import com.example.my_store.delivery.controller.dto.CreateDeliveryDto;
import com.example.my_store.delivery.controller.dto.GetDeliveryDto;
import com.example.my_store.delivery.controller.dto.UpdateDeliveryDto;
import com.example.my_store.delivery.repository.entity.DeliveryEntity;
import com.example.my_store.order.repository.order.entity.OrderEntity;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface DeliveryEntityMapper {
    @Mapping(source = "courierId", target = "courier.id")
    @Mapping(source = "orderId", target = "order.id")
    DeliveryEntity convertToEntity(GetDeliveryDto getDeliveryDto);

    @Mapping(source = "courier.id", target = "courierId")
    @Mapping(source = "order.id", target = "orderId")
    GetDeliveryDto convertToGetDeliveryDto(DeliveryEntity deliveryEntity);

    @Mapping(target = "order", ignore = true)
    @Mapping(source = "courierId", target = "courier")
    DeliveryEntity updateWithNull(UpdateDeliveryDto updateDeliveryDto, @MappingTarget DeliveryEntity deliveryEntity);

    default OrderEntity createOrderEntity(Long orderId) {
        if (orderId == null) {
            return null;
        }
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(orderId);
        return orderEntity;
    }

    default CourierEntity createCourierEntity(Long courierId) {
        if (courierId == null) {
            return null;
        }
        CourierEntity courierEntity = new CourierEntity();
        courierEntity.setId(courierId);
        return courierEntity;
    }

    @Mapping(source = "courierId", target = "courier.id")
    @Mapping(source = "orderId", target = "order.id")
    DeliveryEntity convertToEntity(CreateDeliveryDto createDeliveryDto);
}