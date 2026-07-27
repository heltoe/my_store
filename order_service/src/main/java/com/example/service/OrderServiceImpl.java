package com.example.service;

import com.example.common_lib.config.ServiceUrlsProperties;
import com.example.common_lib.dto.GetAccountDto;
import com.example.common_lib.dto.GetProductDto;
import com.example.common_lib.dto.GetOrderDto;
import com.example.common_lib.dto.STATE_ORDER;
import com.example.controller.dto.CreateOrderDto;
import com.example.controller.dto.CreateOrderItemDto;
import com.example.repository.order.OrderRepository;
import com.example.repository.order.entity.OrderEntity;
import com.example.repository.order_item.entity.OrderItemEntity;
import com.example.utils.OrderEntityMapper;
import com.example.utils.OrderItemEntityMapper;
import com.example.common_lib.utils.exception.CommonConflictException;
import com.example.common_lib.utils.exception.CommonEntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {
    private final WebClient webClient;

    private final ServiceUrlsProperties serviceUrls;

    private final OrderEntityMapper orderEntityMapper;

    private final OrderItemEntityMapper orderItemEntityMapper;

    private final OrderRepository orderRepository;

    private OrderEntity getRequiredOrder(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new CommonEntityNotFoundException("Order with id `%s` not found".formatted(id)));
    }

    public GetAccountDto getRequiredAccount(Long id) {
        return webClient
                .get()
                .uri(serviceUrls.getAccount() + "/rest/accounts/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.error(new CommonEntityNotFoundException("Account with id `%s` not found".formatted(id))))
                .bodyToMono(GetAccountDto.class)
                .block();
    }

    public GetProductDto getRequiredProduct(Long id) {
        return webClient
                .get()
                .uri(serviceUrls.getProducts() + "/rest/products/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.error(new CommonEntityNotFoundException("Product with id `%s` not found".formatted(id))))
                .bodyToMono(GetProductDto.class)
                .block();
    }

    private void throwIfOrderStatusIsNot(STATE_ORDER incomeStatus, STATE_ORDER correctStatus) {
        if (incomeStatus != correctStatus) {
            throw new CommonConflictException("Order status must be `%s`".formatted(correctStatus));
        }
    }

    private OrderItemEntity createOrderItemEntity(CreateOrderItemDto dto, OrderEntity orderEntity) {
        GetProductDto productEntity = getRequiredProduct(dto.productId());
        OrderItemEntity orderItemEntity = orderItemEntityMapper.convertToEntity(dto);
        orderItemEntity.setOrder(orderEntity);
        orderItemEntity.setProductId(productEntity.id());
        orderItemEntity.setPrice(productEntity.price());
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
        GetAccountDto accountEntity = getRequiredAccount(dto.orderDto().accountId());
        OrderEntity orderEntity = orderEntityMapper.convertToEntity(dto.orderDto());
        orderEntity.setAccountId(accountEntity.id());
        orderEntity.setStatus(STATE_ORDER.CREATED);

        List<OrderItemEntity> listOrderItemsEntity = dto.items().stream()
                .map(item -> createOrderItemEntity(item, orderEntity))
                .toList();
        orderEntity.setProducts(listOrderItemsEntity);

        OrderEntity resultOrderEntity = orderRepository.save(orderEntity);
        return orderEntityMapper.convertToGetOrderDto(resultOrderEntity);
    }

    @Transactional
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
