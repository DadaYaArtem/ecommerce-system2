package org.example.model;

public class OrderInfo {

    private final String productId;
    private final int quantity;
    private final String customerId;

    public OrderInfo(String productId, int quantity, String customerId) {
        this.productId = productId;
        this.quantity = quantity;
        this.customerId = customerId;
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
}
