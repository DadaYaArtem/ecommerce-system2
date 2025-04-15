package org.example.events;

public class PaymentRequestEvent {

    private String orderId;
    private String productId;
    private int quantity;
    private String customerId;
    private double price; // ← НОВОЕ ПОЛЕ

    public PaymentRequestEvent() {}

    public PaymentRequestEvent(String orderId, String productId, int quantity, String customerId, double price) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.customerId = customerId;
        this.price = price;
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

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "💳 PaymentRequestEvent{" +
                "orderId='" + orderId + '\'' +
                ", productId='" + productId + '\'' +
                ", quantity=" + quantity +
                ", customerId='" + customerId + '\'' +
                ", price=" + price +
                '}';
    }
}
