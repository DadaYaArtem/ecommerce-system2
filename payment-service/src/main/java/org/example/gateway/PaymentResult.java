package org.example.gateway;

import java.time.LocalDateTime;

public class PaymentResult {
    private String transactionId;
    private PaymentStatus status;
    private LocalDateTime timestamp;
    private String errorMessage; // nullable

    public PaymentResult(String transactionId, PaymentStatus status, LocalDateTime timestamp) {
        this(transactionId, status, timestamp, null);
    }

    public PaymentResult(String transactionId, PaymentStatus status, LocalDateTime timestamp, String errorMessage) {
        this.transactionId = transactionId;
        this.status = status;
        this.timestamp = timestamp;
        this.errorMessage = errorMessage;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}