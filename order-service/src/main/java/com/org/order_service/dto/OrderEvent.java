package com.org.order_service.dto;

import com.org.order_service.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    private String orderId;

    private Long userId;

    private Long productId;

    private Integer quantity;

    private String productName;

    private Double price;

    private Double sale;

    private Double totalPrice;

    private OrderStatus status;
}
