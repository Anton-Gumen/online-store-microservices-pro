package com.org.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    private String orderId;
    private Long userId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double price;
    private Double sale;
    private Double totalPrice;
    private String status;
}
