package com.example.my_store.order.service;

import com.example.my_store.account.repository.AccountRepository;
import com.example.my_store.account.repository.entity.AccountEntity;
import com.example.my_store.order.controller.dto.CreateOrderItemDto;
import com.example.my_store.order.controller.dto.CreateOrderDto;
import com.example.my_store.order.controller.dto.GetOrderDto;
import com.example.my_store.order.repository.order.OrderRepository;
import com.example.my_store.order.repository.order.entity.OrderEntity;
import com.example.my_store.order.repository.order.entity.STATE_ORDER;
import com.example.my_store.order.repository.order_item.entity.OrderItemEntity;
import com.example.my_store.order.utils.OrderEntityMapper;
import com.example.my_store.order.utils.OrderItemEntityMapper;
import com.example.my_store.product.repository.entity.ProductEntity;
import com.example.my_store.product.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.my_store.utils.exception.CommonEntityNotFoundException;
import com.example.my_store.utils.exception.CommonConflictException;

import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderEntityMapper orderEntityMapper;

    private final OrderItemEntityMapper orderItemEntityMapper;

    private final OrderRepository orderRepository;

    private final AccountRepository accountRepository;

    private final ProductService productService;

    private OrderEntity getRequiredOrder(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new CommonEntityNotFoundException("Order with id `%s` not found".formatted(id)));
    }

    private AccountEntity getRequiredAccount(Long id) {
        return accountRepository.findById(id).orElseThrow(() -> new CommonEntityNotFoundException("Account with id `%s` not found".formatted(id)));
    }

    private void throwIfOrderStatusIsNot(STATE_ORDER incomeStatus, STATE_ORDER correctStatus) {
        if (incomeStatus != correctStatus) {
            throw new CommonConflictException("Order status must be `%s`".formatted(correctStatus));
        }
    }

    private OrderItemEntity createOrderItemEntity(CreateOrderItemDto dto, OrderEntity orderEntity) {
        ProductEntity productEntity = productService.getRequiredActiveProduct(dto.productId());
        OrderItemEntity orderItemEntity = orderItemEntityMapper.convertToEntity(dto);
        orderItemEntity.setOrder(orderEntity);
        orderItemEntity.setProduct(productEntity);
        orderItemEntity.setPrice(productEntity.getPrice());
        return orderItemEntity;
    }

    @Override
    public Page<GetOrderDto> getAll(Pageable pageable) {
        Page<OrderEntity> orderEntities = orderRepository.findAll(pageable);
        return orderEntities.map(orderEntityMapper::convertToGetOrderDto);
    }

    @Override
    public GetOrderDto getOne(Long id) {
        OrderEntity entity = getRequiredOrder(id);
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
        AccountEntity accountEntity = getRequiredAccount(dto.orderDto().accountId());
        OrderEntity orderEntity = orderEntityMapper.convertToEntity(dto.orderDto());
        orderEntity.setAccount(accountEntity);
        orderEntity.setStatus(STATE_ORDER.CREATED);

        List<OrderItemEntity> listOrderItemsEntity = dto.items().stream()
                .map(item -> createOrderItemEntity(item, orderEntity))
                .toList();
        orderEntity.setItems(listOrderItemsEntity);

        OrderEntity resultOrderEntity = orderRepository.save(orderEntity);
        return orderEntityMapper.convertToGetOrderDto(resultOrderEntity);
    }

    @Override
    public void changeOrderState(Long idOrder, STATE_ORDER status) {
        OrderEntity entity = getRequiredOrder(idOrder);
        switch (status) {
            case ACCEPTED -> {
                throwIfOrderStatusIsNot(entity.getStatus(), STATE_ORDER.CREATED);
                entity.setStatus(STATE_ORDER.ACCEPTED);
                orderRepository.save(entity);
            }
            case PAID -> {
                throwIfOrderStatusIsNot(entity.getStatus(), STATE_ORDER.ACCEPTED);
                entity.setStatus(STATE_ORDER.PAID);
                orderRepository.save(entity);
            }
            case WAIT_BIND_TO_COURIER -> {
                throwIfOrderStatusIsNot(entity.getStatus(), STATE_ORDER.PAID);
                entity.setStatus(STATE_ORDER.WAIT_BIND_TO_COURIER);
                orderRepository.save(entity);
            }
            case DELIVERY_ON_THE_WAY -> {
                throwIfOrderStatusIsNot(entity.getStatus(), STATE_ORDER.WAIT_BIND_TO_COURIER);
                entity.setStatus(STATE_ORDER.DELIVERY_ON_THE_WAY);
                orderRepository.save(entity);
            }
            case RECEIVED_BY_USER -> {
                throwIfOrderStatusIsNot(entity.getStatus(), STATE_ORDER.DELIVERY_ON_THE_WAY);
                entity.setStatus(STATE_ORDER.RECEIVED_BY_USER);
                orderRepository.save(entity);
            }
            case CANCELED -> {
                if (entity.getStatus() == STATE_ORDER.RECEIVED_BY_USER) {
                    throw new CommonConflictException("Order with status `%s` cannot be canceled".formatted(STATE_ORDER.RECEIVED_BY_USER));
                }
                if (entity.getStatus() == STATE_ORDER.CANCELED) {
                    throw new CommonConflictException("Order with status `%s` cannot be canceled".formatted(STATE_ORDER.CANCELED));
                }
                entity.setStatus(STATE_ORDER.CANCELED);
                orderRepository.save(entity);
            }
        }
    }
}
