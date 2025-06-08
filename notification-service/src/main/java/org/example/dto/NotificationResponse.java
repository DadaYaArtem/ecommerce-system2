package org.example.dto;

import java.time.LocalDateTime;

public class NotificationResponse {
    private String id;
    private String orderId;
    private String message;
    private LocalDateTime createdAt;

    public NotificationResponse(String id, String orderId, String message, LocalDateTime createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.message = message;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
