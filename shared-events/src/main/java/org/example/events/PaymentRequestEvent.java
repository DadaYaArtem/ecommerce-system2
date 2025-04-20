package org.example.events;

public class PaymentRequestEvent {

    private String orderId;
    private double totalAmount;
    private String customerId;

    public PaymentRequestEvent() {}

    public PaymentRequestEvent(String orderId, double totalAmount, String customerId) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.customerId = customerId;
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

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    @Override
    public String toString() {
        return "PaymentRequestEvent{" +
                "orderId='" + orderId + '\'' +
                ", totalAmount=" + totalAmount +
                ", customerId='" + customerId + '\'' +
                '}';
    }
}
