package com.org.order_service.kafka;

import com.org.order_service.dto.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderKafkaProducer {


    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private static final String TOPIC_NAME = "orders";

    public void sendOrderEvent(OrderEvent event) {
        kafkaTemplate.send(TOPIC_NAME, event.getOrderId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Order event sent: orderId={}, offset={}",
                                event.getOrderId(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send order event: {}", ex.getMessage(), ex);
                    }
                });
    }
}