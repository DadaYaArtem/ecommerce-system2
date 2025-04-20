package org.example.events;

public class PaymentFailedEvent {

    private String orderId;
    private double totalAmount;

    public PaymentFailedEvent() {
    }

    public PaymentFailedEvent(String orderId, double totalAmount) {
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
        return "PaymentFailedEvent{" +
                "orderId='" + orderId + '\'' +
                ", totalAmount=" + totalAmount +
                '}';
    }
}
