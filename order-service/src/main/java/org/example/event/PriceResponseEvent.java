package org.example.event;

public class PriceResponseEvent {
    private String orderId;
    private String productId;
    private double price;

    public PriceResponseEvent() {}

    public PriceResponseEvent(String orderId, String productId, double price) {
        this.orderId = orderId;
        this.productId = productId;
        this.price = price;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getProductId() {
        return productId;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "💵 PriceResponseEvent{" +
                "orderId='" + orderId + '\'' +
                ", productId='" + productId + '\'' +
                ", price=" + price +
                '}';
    }
}
