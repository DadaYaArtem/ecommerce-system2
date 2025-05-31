package org.example.dto;

public class InventoryResponse {
    private String productId;
    private String warehouseId;
    private int quantity;

    public InventoryResponse(String productId, String warehouseId, int quantity) {
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public int getQuantity() {
        return quantity;
    }
} 
