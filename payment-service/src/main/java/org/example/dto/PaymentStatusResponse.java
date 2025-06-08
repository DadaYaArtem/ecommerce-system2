package org.example.dto;

import org.example.gateway.PaymentStatus;

public class PaymentStatusResponse {
    private String orderId;
    private PaymentStatus status;
    private String transactionId;
    private String errorMessage;

    public PaymentStatusResponse(String orderId, PaymentStatus status, String transactionId, String errorMessage) {
        this.orderId = orderId;
        this.status = status;
        this.transactionId = transactionId;
        this.errorMessage = errorMessage;
    }

    public String getOrderId() {
        return orderId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
