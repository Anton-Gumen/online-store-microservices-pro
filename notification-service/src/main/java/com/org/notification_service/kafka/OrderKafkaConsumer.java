package com.org.notification_service.kafka;

import com.org.notification_service.dto.OrderEvent;
import com.org.notification_service.entity.Order;
import com.org.notification_service.entity.OrderStatus;
import com.org.notification_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderKafkaConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "orders", groupId = "notification-group")
    public void listen(OrderEvent event) {
        log.info("Received order event from Kafka: {}", event.getOrderId());

        // Маппим DTO в JPA-сущность
        Order order = Order.builder()
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .productId(event.getProductId())
                .productName(event.getProductName())
                .quantity(event.getQuantity())
                .price(event.getPrice())
                .sale(event.getSale())
                .totalPrice(event.getTotalPrice())
                .status(OrderStatus.PENDING)
                .build();

        orderRepository.save(order);
        log.info("Order successfully saved to database: orderId={}, userId={}", order.getOrderId(), order.getUserId());
    }
}