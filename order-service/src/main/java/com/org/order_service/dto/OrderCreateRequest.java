package com.org.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderCreateRequest {

    private Long productId;

    private Integer quantity;
}
