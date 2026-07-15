package com.example.my_store.payment.utils;

import com.example.my_store.order.repository.order.entity.OrderEntity;
import com.example.my_store.payment.controller.dto.CreatePaymentDto;
import com.example.my_store.payment.controller.dto.GetPaymentDto;
import com.example.my_store.payment.repository.entity.PaymentEntity;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentEntityMapper {
    @Mapping(source = "order.id", target = "orderId")
    GetPaymentDto convertToGetPaymentDto(PaymentEntity paymentEntity);

    @Mapping(source = "orderId", target = "order.id")
    PaymentEntity convertToEntity(CreatePaymentDto createPaymentDto);

    default OrderEntity createOrderEntity(Long orderId) {
        if (orderId == null) {
            return null;
        }
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(orderId);
        return orderEntity;
    }
}
