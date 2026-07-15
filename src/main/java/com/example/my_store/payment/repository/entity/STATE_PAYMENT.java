package com.example.my_store.payment.repository.entity;

public enum STATE_PAYMENT {
    /**
     * Ожидает оплаты
     */
    PENDING,
    /**
     * Успех
     */
    SUCCESS,
    /**
     * Провал
     */
    FAILURE,
}
