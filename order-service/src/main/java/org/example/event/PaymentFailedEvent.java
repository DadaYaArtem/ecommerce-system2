package org.example.event;

public class PaymentFailedEvent {

    private String orderId;
    private String reason;

    public PaymentFailedEvent() {
    }

    public PaymentFailedEvent(String orderId, String reason) {
        this.orderId = orderId;
        this.reason = reason;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "❌ PaymentFailedEvent{" +
                "orderId='" + orderId + '\'' +
                ", reason='" + reason + '\'' +
                '}';
    }
}
