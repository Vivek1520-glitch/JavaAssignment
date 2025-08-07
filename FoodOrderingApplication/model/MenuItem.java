package com.aurionpro.model;

public class MenuItem {
    private int itemId;
    private String name;
    private double price;
    private int stockQuantity;

    public MenuItem(String name, double price, int stockQuantity) {
        if (price <= 0) throw new IllegalArgumentException("Price must be positive!");
        if (stockQuantity < 0) throw new IllegalArgumentException("Stock quantity cannot be negative!");
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public MenuItem(int itemId, String name, double price, int stockQuantity) {
        this(name, price, stockQuantity);
        this.itemId = itemId;
    }

    public int getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        if (stockQuantity < 0) throw new IllegalArgumentException("Stock quantity cannot be negative!");
        this.stockQuantity = stockQuantity;
    }
}