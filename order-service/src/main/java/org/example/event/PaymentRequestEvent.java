package org.example.event;

public class PaymentRequestEvent {

    private String orderId;
    private String productId;
    private int quantity;
    private String customerId;

    public PaymentRequestEvent() {
    }

    public PaymentRequestEvent(String orderId, String productId, int quantity, String customerId) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.customerId = customerId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getCustomerId() {
        return customerId;
    }

    @Override
    public String toString() {
        return "💳 PaymentRequestEvent{" +
                "orderId='" + orderId + '\'' +
                ", productId='" + productId + '\'' +
                ", quantity=" + quantity +
                ", customerId='" + customerId + '\'' +
                '}';
    }
}
