package org.example.events;

public class ReleaseInventoryEvent {
    private String orderId;
    private String productId;
    private int quantity;

    public ReleaseInventoryEvent() {}

    public ReleaseInventoryEvent(String orderId, String productId, int quantity) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
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

    @Override
    public String toString() {
        return "ReleaseInventoryEvent{" +
                "orderId='" + orderId + '\'' +
                ", productId='" + productId + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}