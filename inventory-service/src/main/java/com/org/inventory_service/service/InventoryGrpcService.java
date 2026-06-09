package com.org.inventory_service.service;

import com.org.inventory.grpc.InventoryServiceGrpc;
import com.org.inventory.grpc.ProductProto;
import com.org.inventory_service.repository.ProductRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class InventoryGrpcService extends InventoryServiceGrpc.InventoryServiceImplBase {

    private final ProductRepository productRepository;

    @Override
    public void checkAvailability(ProductProto.ProductRequest request,
                                  StreamObserver<ProductProto.ProductResponse> responseObserver) {

        productRepository.findById(request.getProductId())
                .ifPresentOrElse(
                        product -> {
                            var response = ProductProto.ProductResponse.newBuilder()
                                    .setId(product.getId())
                                    .setName(product.getName())
                                    .setQuantity(product.getQuantity())
                                    .setPrice(product.getPrice())
                                    .setSale(product.getSale())
                                    .build();
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                        },
                        () -> {
                            responseObserver.onError(
                                    io.grpc.Status.NOT_FOUND
                                            .withDescription("Product not found: " + request.getProductId())
                                            .asRuntimeException()
                            );
                        }
                );
    }


}
