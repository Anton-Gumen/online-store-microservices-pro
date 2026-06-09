package com.org.order_service.controller;

import com.org.order.grpc.ProductProto;
import com.org.order_service.dto.OrderRequest;
import com.org.order_service.dto.OrderResponse;
import com.org.order_service.service.InventoryGrpcClientService;
import com.org.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final InventoryGrpcClientService inventoryGrpcClientService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody OrderRequest request
            ) {

        OrderResponse response = orderService.createOrder(
                userDetails.getUsername(),
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<?> getProductById(@PathVariable Long productId) {
        try {
            ProductProto.ProductResponse product = inventoryGrpcClientService.checkProductAvailability(productId);

            return ResponseEntity.ok(Map.of(
                    "id", product.getId(),
                    "name", product.getName(),
                    "quantity", product.getQuantity(),
                    "price", product.getPrice(),
                    "sale", product.getSale()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Product not found with id: " + productId));
        }
    }
}
