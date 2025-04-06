// 📁 order-service/src/main/java/org/example/store/OrderStatusStore.java
package org.example.store;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrderStatusStore {
    private final Map<String, String> statusMap = new ConcurrentHashMap<>();

    public void setStatus(String orderId, String status) {
        statusMap.put(orderId, status);
    }

    public String getStatus(String orderId) {
        return statusMap.getOrDefault(orderId, "UNKNOWN");
    }
}
