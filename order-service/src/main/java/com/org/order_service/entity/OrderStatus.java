package com.org.order_service.entity;

public enum OrderStatus {
    PENDING,      // Ожидает обработки
    CONFIRMED,    // Подтвержден
    PROCESSING,   // В обработке
    SHIPPED,      // Отправлен
    DELIVERED,    // Доставлен
    CANCELLED,    // Отменен
    REFUNDED      // Возвращен
}
