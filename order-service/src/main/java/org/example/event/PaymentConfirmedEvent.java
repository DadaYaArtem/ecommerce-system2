package org.example.event;

public class PaymentConfirmedEvent {

    private String orderId;
    private double amount;

    public PaymentConfirmedEvent() {
    }

    public PaymentConfirmedEvent(String orderId, double amount) {
        this.orderId = orderId;
        this.amount = amount;
    }

    public String getOrderId() {
        return orderId;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "✅ PaymentConfirmedEvent{" +
                "orderId='" + orderId + '\'' +
                ", amount=" + amount +
                '}';
    }
}
