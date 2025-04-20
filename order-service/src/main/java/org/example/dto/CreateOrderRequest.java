package org.example.dto;

import java.util.List;

public class CreateOrderRequest {
    private String customerId;
    private List<ItemRequest> items;

    public String getCustomerId() {
        return customerId;
    }

    public List<ItemRequest> getItems() {
        return items;
    }

    public static class ItemRequest {
        private String productId;
        private int quantity;

        public String getProductId() {
            return productId;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}
