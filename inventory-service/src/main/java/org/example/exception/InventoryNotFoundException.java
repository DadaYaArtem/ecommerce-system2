package org.example.exception;

public class InventoryNotFoundException extends RuntimeException {
    public InventoryNotFoundException(String productId, String warehouseId) {
        super("Inventory not found for productId=" + productId + " at warehouseId=" + warehouseId);
    }
}