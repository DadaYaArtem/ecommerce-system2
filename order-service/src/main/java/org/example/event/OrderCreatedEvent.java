package org.example.event;

public class OrderCreatedEvent {
    private String orderId;
    private String productId;
    private int quantity;
    private String customerId;

    public OrderCreatedEvent() {}

    public OrderCreatedEvent(String orderId, String productId, int quantity, String customerId) {
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
        return "📦 OrderCreatedEvent{" +
                "orderId='" + orderId + '\'' +
                ", productId='" + productId + '\'' +
                ", quantity=" + quantity +
                ", customerId='" + customerId + '\'' +
                '}';
    }
}
