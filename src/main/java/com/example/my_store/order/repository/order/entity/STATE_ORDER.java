package com.example.my_store.order.repository.order.entity;

public enum STATE_ORDER {
    /**
     * Создан
     */
    CREATED,
    /**
     * Подтвержден
     */
    ACCEPTED,
    /**
     * Оплачен
     */
    PAID,
    /**
     * Ждет прикрепления к курьеру
     */
    WAIT_BIND_TO_COURIER,
    /**
     * В пути
     */
    DELIVERY_ON_THE_WAY,
    /**
     * Получен клиентом
     */
    RECEIVED_BY_USER,
    /**
     * Отменен
     */
    CANCELED,
}
