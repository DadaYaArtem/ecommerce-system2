package org.example.dto;

public class WarehouseInfo {
    private String warehouseId;
    private String name;

    public WarehouseInfo(String warehouseId, String name) {
        this.warehouseId = warehouseId;
        this.name = name;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public String getName() {
        return name;
    }
}
