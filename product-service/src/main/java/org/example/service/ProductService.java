package org.example.service;

import org.example.dto.ProductResponse;
import org.example.exception.ProductNotFoundException;
import org.example.model.Product;
import org.example.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

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

    public BigDecimal getPriceByProductId(String id) {
        return BigDecimal.valueOf(repository.findById(id)
                .map(Product::getPrice)
                .orElseThrow(() -> new ProductNotFoundException(id)));
    }

    public List<ProductResponse> getAllProducts() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse getProductById(String id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public List<ProductResponse> search(String name, String category) {
        return repository.findAll().stream()
                .filter(p -> name == null || p.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(p -> category == null || p.getCategory().toLowerCase().contains(category.toLowerCase()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(), p.getName(), p.getDescription(), p.getCategory(),
                p.getBrand(), p.getSupplier(), p.getPrice(),
                p.getCurrency(), p.isAvailable()
        );
    }
}