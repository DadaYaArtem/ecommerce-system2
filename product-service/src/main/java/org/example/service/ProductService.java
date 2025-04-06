package org.example.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ProductService {

    private final Map<String, Double> priceMap = Map.of(
            "p123", 499.0,
            "p456", 1299.0,
            "p789", 9999.0
    );

    public double getPrice(String productId) {
        return priceMap.getOrDefault(productId, 0.0);
    }
}
