package com.org.order_service.service;

import com.org.order.grpc.ProductProto;
import com.org.order_service.dto.OrderEvent;
import com.org.order_service.dto.OrderRequest;
import com.org.order_service.dto.OrderResponse;
import com.org.order_service.entity.Order;
import com.org.order_service.entity.OrderStatus;
import com.org.order_service.kafka.OrderKafkaProducer;
import com.org.order_service.repository.OrderRepository;
import com.org.order_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryGrpcClientService inventoryGrpcClientService;
    private final UserRepository userRepository;
    private final OrderKafkaProducer orderKafkaProducer;

    @Transactional
    public OrderResponse createOrder(String username, OrderRequest request) {
        // 1. Получаем userId
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username))
                .getId();

        // 2. Проверяем товар через gRPC
        ProductProto.ProductResponse product =
                inventoryGrpcClientService.checkProductAvailability(request.getProductId());

        // 3. Проверяем наличие
        if (product.getQuantity() < request.getQuantity()) {
            throw new RuntimeException("Not enough stock. Available: " + product.getQuantity());
        }

        // 4. Рассчитываем цену
        Double totalPrice = calculatePrice(product.getPrice(), product.getSale(), request.getQuantity());

        // 5. СОХРАНЯЕМ В БД
        Order order = Order.builder()
                .orderNumber(UUID.randomUUID().toString())
                .userId(userId)
                .productId(product.getId())
                .productName(product.getName())
                .quantity(request.getQuantity())
                .price(product.getPrice())
                .sale(product.getSale())
                .totalPrice(totalPrice)
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Order saved to DB: id={}, orderNumber={}", savedOrder.getId(), savedOrder.getOrderNumber());

        // 6. ОТПРАВЛЯЕМ В KAFKA
        OrderEvent orderEvent = OrderEvent.builder()
                .orderId(savedOrder.getOrderNumber())
                .userId(userId)
                .productId(product.getId())
                .productName(product.getName())
                .quantity(request.getQuantity())
                .price(product.getPrice())
                .sale(product.getSale())
                .totalPrice(totalPrice)
                .status(OrderStatus.PENDING)
                .build();

        orderKafkaProducer.sendOrderEvent(orderEvent);

        // 7. Возвращаем ответ
        return OrderResponse.builder()
                .orderId(savedOrder.getOrderNumber())
                .productName(product.getName())
                .quantity(request.getQuantity())
                .totalPrice(totalPrice)
                .status(savedOrder.getStatus().toString())
                .message("Order created and event sent to Kafka")
                .build();
    }

    private Double calculatePrice(Double price, Double sale, Integer quantity) {
        if (sale != null && sale > 0) {
            double discountedPrice = price * (1 - sale / 100);
            return discountedPrice * quantity;
        }
        return price * quantity;
    }
}