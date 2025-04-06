package org.example.listener;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrderInfoStore {
    private final Map<String, Integer> orderQuantities = new ConcurrentHashMap<>();
    
    public void setQuantity(String orderId, int quantity) {
        orderQuantities.put(orderId, quantity);
    }
    
    public int getQuantity(String orderId) {
        return orderQuantities.getOrDefault(orderId, 1); // 1 по умолчанию
    }
}