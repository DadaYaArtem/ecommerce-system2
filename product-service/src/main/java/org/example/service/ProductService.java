package org.example.service;

import org.example.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public double getPrice(String productId) {
        return repository.findById(productId)
                .map(product -> {
                    System.out.println("✅ Ціна з бази: " + product.getPrice());
                    return product.getPrice();
                })
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
    }
}