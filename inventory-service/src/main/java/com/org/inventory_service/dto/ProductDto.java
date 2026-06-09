package com.org.inventory_service.dto;

import lombok.Data;

@Data
public class ProductDto {

    private Long id;

    private String name;

    private Integer quantity;

    private Double price;

    private Double sale;
}
