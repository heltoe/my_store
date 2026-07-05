package com.example.my_store.payment.service;

import com.example.my_store.payment.controller.dto.CreateOrUpdatePaymentDto;
import com.example.my_store.payment.controller.dto.GetPaymentDto;
import com.example.my_store.payment.repository.PaymentRepository;
import com.example.my_store.payment.repository.entity.PaymentEntity;
import com.example.my_store.payment.repository.entity.STATE_PAYMENT;
import com.example.my_store.payment.utils.PaymentEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentEntityMapper paymentEntityMapper;

    private final PaymentRepository paymentRepository;

    private PaymentEntity _getOne(Long id) {
        return paymentRepository.findByOrder_Id(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));
    }

    @Override
    public Page<GetPaymentDto> getAll(Pageable pageable) {
        Page<PaymentEntity> paymentEntities = paymentRepository.findAll(pageable);
        return paymentEntities.map(paymentEntityMapper::convertToGetPaymentDto);
    }

    @Override
    public GetPaymentDto getOne(Long id) {
        PaymentEntity entity = _getOne(id);
        return paymentEntityMapper.convertToGetPaymentDto(entity);
    }

    @Override
    public List<GetPaymentDto> getMany(List<Long> ids) {
        List<PaymentEntity> paymentEntities = paymentRepository.findAllById(ids);
        return paymentEntities.stream()
                .map(paymentEntityMapper::convertToGetPaymentDto)
                .toList();
    }

    @Override
    public GetPaymentDto create(CreateOrUpdatePaymentDto dto) {
        PaymentEntity entity = _getOne(dto.id());
        entity.setStatus(STATE_PAYMENT.FAILURE);
        PaymentEntity resultPaymentEntity = paymentRepository.save(entity);
        return paymentEntityMapper.convertToGetPaymentDto(resultPaymentEntity);
    }

    @Override
    public GetPaymentDto patch(Long id) {
        PaymentEntity entity = _getOne(id);
        entity.setStatus(STATE_PAYMENT.SUCCESS);
        PaymentEntity resultPaymentEntity = paymentRepository.save(entity);
        return paymentEntityMapper.convertToGetPaymentDto(resultPaymentEntity);
    }
}
