package com.org.inventory_service.controller;

import com.org.inventory_service.entity.Product;
import com.org.inventory_service.repository.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;

    @GetMapping
    public List<Product> getProductsAll() {

        return productRepository.findAll();

    }

    @GetMapping("{id}")
    public Product getProductId(@PathVariable Long id) {

        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found..."));
    }

    @PostMapping
    public ResponseEntity<Product> createdProduct(@Valid @RequestBody Product product) {

        if (product.getId() != null && productRepository.existsById(product.getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Product savedProduct = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);

    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteProductById(@PathVariable Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        productRepository.delete(product);

        return ResponseEntity.noContent().build();

    }
}
