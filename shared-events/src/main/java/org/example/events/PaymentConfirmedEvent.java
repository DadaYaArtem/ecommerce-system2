package org.example.events;

public class PaymentConfirmedEvent {

    private String orderId;
    private double totalAmount;

    public PaymentConfirmedEvent() {
    }

    public PaymentConfirmedEvent(String orderId, double totalAmount) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
    }

    public String getOrderId() {
        return orderId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    @Override
    public String toString() {
        return "✅ PaymentConfirmedEvent{" +
                "orderId='" + orderId + '\'' +
                ", totalAmount=" + totalAmount +
                '}';
    }
}
