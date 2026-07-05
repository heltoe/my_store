package com.example.my_store.order.service;

import com.example.my_store.order.controller.dto.ChangeOrderStatusDto;
import com.example.my_store.order.controller.dto.CreateOrderDto;
import com.example.my_store.order.controller.dto.GetOrderDto;
import com.example.my_store.order.repository.order.OrderRepository;
import com.example.my_store.order.repository.order.entity.OrderEntity;
import com.example.my_store.order.repository.order.entity.STATE_ORDER;
import com.example.my_store.order.repository.order_item.OrderItemRepository;
import com.example.my_store.order.repository.order_item.entity.OrderItemEntity;
import com.example.my_store.order.utils.OrderEntityMapper;
import com.example.my_store.order.utils.OrderItemEntityMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderEntityMapper orderEntityMapper;

    private final OrderItemEntityMapper orderItemEntityMapper;

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    private OrderEntity _getOneOrder(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order with id `%s` not found".formatted(id)));
    }

    private void _throwOrderStatusError(STATE_ORDER incomeStatus, STATE_ORDER correctStatus) throws ResponseStatusException {
        if (incomeStatus != correctStatus) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order status must be `%s`".formatted(correctStatus));
        }
    }

    @Override
    public Page<GetOrderDto> getAll(Pageable pageable) {
        Page<OrderEntity> orderEntities = orderRepository.findAll(pageable);
        return orderEntities.map(orderEntityMapper::convertToGetOrderDto);
    }

    @Override
    public GetOrderDto getOne(Long id) {
        OrderEntity entity = _getOneOrder(id);
        return orderEntityMapper.convertToGetOrderDto(entity);
    }

    @Override
    public List<GetOrderDto> getMany(List<Long> ids) {
        List<OrderEntity> orderEntities = orderRepository.findAllById(ids);
        return orderEntities.stream()
                .map(orderEntityMapper::convertToGetOrderDto)
                .toList();
    }

    @Transactional
    @Override
    public GetOrderDto create(CreateOrderDto dto) {
        OrderEntity orderEntity = orderEntityMapper.convertToEntity(dto.orderDto());
        orderEntity.setStatus(STATE_ORDER.CREATED);
        OrderEntity resultOrderEntity = orderRepository.save(orderEntity);

        List <OrderItemEntity> listOrderItemsEntity = dto.items().stream().map(orderItemEntityMapper::convertToEntity).toList();
        orderItemRepository.saveAll(listOrderItemsEntity);

        resultOrderEntity.setItems(listOrderItemsEntity);
        return orderEntityMapper.convertToGetOrderDto(resultOrderEntity);
    }

    @Override
    public void changeOrderState(Long idOrder, STATE_ORDER status) {
        OrderEntity entity = _getOneOrder(idOrder);
        if (entity != null) {
            switch (status) {
                case ACCEPTED -> {
                    _throwOrderStatusError(status, STATE_ORDER.CREATED);
                    entity.setStatus(STATE_ORDER.ACCEPTED);
                    orderRepository.save(entity);
                }
                case PAID -> {
                    _throwOrderStatusError(status, STATE_ORDER.ACCEPTED);
                    entity.setStatus(STATE_ORDER.PAID);
                    orderRepository.save(entity);
                }
                case WAIT_BIND_TO_COURIER -> {
                    _throwOrderStatusError(status, STATE_ORDER.PAID);
                    entity.setStatus(STATE_ORDER.WAIT_BIND_TO_COURIER);
                    orderRepository.save(entity);
                }
                case DELIVERY_ON_THE_WAY -> {
                    _throwOrderStatusError(status, STATE_ORDER.WAIT_BIND_TO_COURIER);
                    entity.setStatus(STATE_ORDER.DELIVERY_ON_THE_WAY);
                    orderRepository.save(entity);
                }
                case RECEIVED_BY_USER -> {
                    _throwOrderStatusError(status, STATE_ORDER.DELIVERY_ON_THE_WAY);
                    entity.setStatus(STATE_ORDER.RECEIVED_BY_USER);
                    orderRepository.save(entity);
                }
                case CANCELED -> {
                    entity.setStatus(STATE_ORDER.CANCELED);
                    orderRepository.save(entity);
                }
            }
        }
    }

    @Override
    public void delete(Long id) {
        OrderEntity entity = _getOneOrder(id);
        if (entity != null) {
            orderRepository.delete(entity);
        }
    }

    @Override
    public void deleteMany(List<Long> ids) {
        orderRepository.deleteAllById(ids);
    }
}
