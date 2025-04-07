package org.example.store;

import org.example.model.OrderInfo;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrderInfoStore {

    private final ConcurrentHashMap<String, OrderInfo> storage = new ConcurrentHashMap<>();

    public void put(String orderId, OrderInfo info) {
        storage.put(orderId, info);
    }

    public OrderInfo get(String orderId) {
        return storage.get(orderId);
    }
}
