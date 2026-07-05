package com.example.my_store.order.controller.dto;


import java.util.List;

public record CreateOrderDto(BaseCreateOrderDto orderDto, List<CreateOrderItemDto> items) {
}
