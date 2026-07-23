package com.example.my_store.payment.service;

import com.example.my_store.order.repository.order.OrderRepository;
import com.example.my_store.order.repository.order.entity.OrderEntity;
import com.example.my_store.order.repository.order.entity.STATE_ORDER;
import com.example.my_store.order.service.OrderService;
import com.example.my_store.payment.controller.dto.CreatePaymentDto;
import com.example.my_store.payment.controller.dto.GetPaymentDto;
import com.example.my_store.payment.repository.PaymentRepository;
import com.example.my_store.payment.repository.entity.PaymentEntity;
import com.example.my_store.payment.repository.entity.STATE_PAYMENT;
import com.example.my_store.payment.utils.PaymentEntityMapper;
import com.example.my_store.utils.exception.CommonConflictException;
import com.example.my_store.utils.exception.CommonEntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentEntityMapper paymentEntityMapper;

    private final PaymentRepository paymentRepository;

    private final OrderRepository orderRepository;

    private final OrderService orderService;

    private PaymentEntity getRequiredPayment(Long id) {
        return paymentRepository.findById(id).orElseThrow(() ->
                new CommonEntityNotFoundException("Payment with id `%s` not found".formatted(id)));
    }

    private PaymentEntity getRequiredPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrder_Id(orderId).orElseThrow(() ->
                new CommonEntityNotFoundException("Payment for order with id `%s` not found".formatted(orderId)));
    }

    private OrderEntity getRequiredOrder(Long id) {
        return orderRepository.findById(id).orElseThrow(() ->
                new CommonEntityNotFoundException("Order with id `%s` not found".formatted(id)));
    }

    private void throwIfOrderStatusIsNot(STATE_ORDER incomeStatus, STATE_ORDER correctStatus) {
        if (incomeStatus != correctStatus) {
            throw new CommonConflictException("Order status must be `%s`".formatted(correctStatus));
        }
    }

    private void throwIfPaymentStatusIsNot(STATE_PAYMENT incomeStatus, STATE_PAYMENT correctStatus) {
        if (incomeStatus != correctStatus) {
            throw new CommonConflictException("Payment status must be `%s`".formatted(correctStatus));
        }
    }

    @Override
    public Page<GetPaymentDto> getAll(Pageable pageable) {
        Page<PaymentEntity> paymentEntities = paymentRepository.findAll(pageable);
        return paymentEntities.map(paymentEntityMapper::convertToGetPaymentDto);
    }

    @Override
    public GetPaymentDto getOne(Long id) {
        PaymentEntity entity = getRequiredPayment(id);
        return paymentEntityMapper.convertToGetPaymentDto(entity);
    }

    @Override
    public GetPaymentDto getByOrderId(Long orderId) {
        PaymentEntity entity = getRequiredPaymentByOrderId(orderId);
        return paymentEntityMapper.convertToGetPaymentDto(entity);
    }

    @Override
    public List<GetPaymentDto> getMany(List<Long> ids) {
        List<PaymentEntity> paymentEntities = paymentRepository.findAllById(ids);
        return paymentEntities.stream()
                .map(paymentEntityMapper::convertToGetPaymentDto)
                .toList();
    }

    @Transactional
    @Override
    public GetPaymentDto create(CreatePaymentDto dto) {
        OrderEntity orderEntity = getRequiredOrder(dto.orderId());
        throwIfOrderStatusIsNot(orderEntity.getStatus(), STATE_ORDER.ACCEPTED);

        Optional<PaymentEntity> existingPayment = paymentRepository.findByOrder_Id(dto.orderId());
        if (existingPayment.isPresent()) {
            throw new CommonConflictException("Payment for order with id `%s` already exists".formatted(dto.orderId()));
        }

        PaymentEntity paymentEntity = paymentEntityMapper.convertToEntity(dto);
        paymentEntity.setStatus(STATE_PAYMENT.PENDING);
        PaymentEntity resultPaymentEntity = paymentRepository.save(paymentEntity);
        return paymentEntityMapper.convertToGetPaymentDto(resultPaymentEntity);
    }

    @Transactional
    @Override
    public GetPaymentDto markSuccess(Long id) {
        PaymentEntity entity = getRequiredPayment(id);
        throwIfPaymentStatusIsNot(entity.getStatus(), STATE_PAYMENT.PENDING);
        entity.setStatus(STATE_PAYMENT.SUCCESS);
        PaymentEntity resultPaymentEntity = paymentRepository.save(entity);
        orderService.changeOrderState(entity.getOrder().getId(), STATE_ORDER.PAID);
        return paymentEntityMapper.convertToGetPaymentDto(resultPaymentEntity);
    }

    @Transactional
    @Override
    public GetPaymentDto markFailure(Long id) {
        PaymentEntity entity = getRequiredPayment(id);
        throwIfPaymentStatusIsNot(entity.getStatus(), STATE_PAYMENT.PENDING);
        entity.setStatus(STATE_PAYMENT.FAILURE);
        PaymentEntity resultPaymentEntity = paymentRepository.save(entity);
        return paymentEntityMapper.convertToGetPaymentDto(resultPaymentEntity);
    }
}
