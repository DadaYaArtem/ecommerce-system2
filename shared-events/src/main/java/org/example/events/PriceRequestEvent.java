package org.example.events;

public class PriceRequestEvent {
    private String orderId;
    private String productId;

    public PriceRequestEvent() {}

    public PriceRequestEvent(String orderId, String productId) {
        this.orderId = orderId;
        this.productId = productId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getProductId() {
        return productId;
    }

    @Override
    public String toString() {
        return "📤 PriceRequestEvent{" +
                "orderId='" + orderId + '\'' +
                ", productId='" + productId + '\'' +
                '}';
    }
}
