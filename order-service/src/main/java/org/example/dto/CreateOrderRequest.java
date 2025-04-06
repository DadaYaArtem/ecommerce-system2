package org.example.dto;

public class CreateOrderRequest {
    private String productId;
    private int quantity;
    private String customerId;

    public CreateOrderRequest() {}

    public CreateOrderRequest(String productId, int quantity, String customerId) {
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
