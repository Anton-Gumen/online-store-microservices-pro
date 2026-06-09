package com.org.order_service.service;

import com.org.order.grpc.InventoryServiceGrpc;
import com.org.order.grpc.ProductProto;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryGrpcClientService {

    @GrpcClient("inventory-service")
    private InventoryServiceGrpc.InventoryServiceBlockingStub inventoryStub;

    public ProductProto.ProductResponse checkProductAvailability(Long productId) {
        ProductProto.ProductRequest request = ProductProto.ProductRequest.newBuilder()
                .setProductId(productId)
                .build();

        try {
            return inventoryStub.checkAvailability(request);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
                throw new RuntimeException("Товар с ID " + productId + " не найден в Inventory Service");
            }
            throw new RuntimeException("Ошибка при обращении к Inventory Service: " + e.getMessage(), e);
        }
    }
}