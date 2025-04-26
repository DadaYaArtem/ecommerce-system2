package org.example.events;

public class PaymentRequestFailedEvent {
    private String orderId;
    private double totalAmount;
    private String errorDetails;

    public PaymentRequestFailedEvent(String orderId, double totalAmount, String errorDetails) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.errorDetails = errorDetails;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    @Override
    public String toString() {
        return "PaymentRequestFailedEvent{" +
                "orderId='" + orderId + '\'' +
                ", totalAmount=" + totalAmount +
                ", errorDetails='" + errorDetails + '\'' +
                '}';
    }
}
