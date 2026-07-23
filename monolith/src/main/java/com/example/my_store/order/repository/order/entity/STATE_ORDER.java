package com.example.my_store.order.repository.order.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статус заказа")
public enum STATE_ORDER {
    @Schema(description = "Создан")
    CREATED,
    @Schema(description = "Подтверждён")
    ACCEPTED,
    @Schema(description = "Оплачен")
    PAID,
    @Schema(description = "Ожидает назначения курьера")
    WAIT_BIND_TO_COURIER,
    @Schema(description = "В пути")
    DELIVERY_ON_THE_WAY,
    @Schema(description = "Получен клиентом")
    RECEIVED_BY_USER,
    @Schema(description = "Отменён")
    CANCELED,
}
