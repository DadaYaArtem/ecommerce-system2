package org.example.dto;

public class ProductResponse {
    private String id;
    private String name;
    private String description;
    private String category;
    private String brand;
    private String supplier;
    private double price;
    private String currency;
    private boolean available;

    public ProductResponse(String id, String name, String description, String category,
                           String brand, String supplier, double price, String currency, boolean available) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.brand = brand;
        this.supplier = supplier;
        this.price = price;
        this.currency = currency;
        this.available = available;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getBrand() {
        return brand;
    }

    public String getSupplier() {
        return supplier;
    }

    public double getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public boolean isAvailable() {
        return available;
    }
} 