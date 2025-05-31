package org.example.controller;

import org.example.model.Inventory;
import org.example.service.InventoryService;
import org.example.dto.InventoryResponse;
import org.example.dto.WarehouseInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // 1. /api/inventory/{productId} — остатки товара по всем складам
    @GetMapping("/{productId}")
    public ResponseEntity<List<InventoryResponse>> getProductStockAcrossWarehouses(@PathVariable String productId) {
        return ResponseEntity.ok(inventoryService.getInventoryByProduct(productId));
    }

    // 2. /api/inventory/warehouses/{warehouseId} — список товаров на складе
    @GetMapping("/warehouses/{warehouseId}")
    public ResponseEntity<List<InventoryResponse>> getProductsByWarehouse(@PathVariable String warehouseId) {
        return ResponseEntity.ok(inventoryService.getInventoryByWarehouse(warehouseId));
    }

    // 3. /api/inventory/{productId}/warehouses — на каких складах хранится товар
    @GetMapping("/{productId}/warehouses")
    public ResponseEntity<List<WarehouseInfo>> getWarehousesByProduct(@PathVariable String productId) {
        return ResponseEntity.ok(inventoryService.getWarehousesWithProduct(productId));
    }

    // 4. /api/inventory/{productId}/warehouse/{warehouseId} — остаток на конкретном складе
    @GetMapping("/{productId}/warehouse/{warehouseId}")
    public ResponseEntity<InventoryResponse> getStockForProductAtWarehouse(
            @PathVariable String productId,
            @PathVariable String warehouseId) {
        return ResponseEntity.ok(inventoryService.getInventoryForProductAtWarehouse(productId, warehouseId));
    }
} 