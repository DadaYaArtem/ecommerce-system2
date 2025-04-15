package org.example.events;

public class InventoryNotAvailableEvent {
    private String orderId;
    private String productId;

    public InventoryNotAvailableEvent() {}

    public InventoryNotAvailableEvent(String orderId, String productId) {
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
        return "❌ InventoryNotAvailableEvent{" +
                "orderId='" + orderId + '\'' +
                ", productId='" + productId + '\'' +
                '}';
    }
}
