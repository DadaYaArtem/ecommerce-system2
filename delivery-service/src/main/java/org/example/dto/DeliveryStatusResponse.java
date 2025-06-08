package org.example.dto;

import org.example.model.DeliveryStatus;

public class DeliveryStatusResponse {
    private String orderId;
    private DeliveryStatus status;
    private String deliveryId;
    private String errorMessage;

    public DeliveryStatusResponse(String orderId, DeliveryStatus status, String deliveryId, String errorMessage) {
        this.orderId = orderId;
        this.status = status;
        this.deliveryId = deliveryId;
        this.errorMessage = errorMessage;
    }

    public String getOrderId() {
        return orderId;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public String getDeliveryId() {
        return deliveryId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
