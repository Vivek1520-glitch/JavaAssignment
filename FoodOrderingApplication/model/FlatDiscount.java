package com.aurionpro.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FlatDiscount implements Discount {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/food_ordering_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1520";
    private double discountAmount;
    private double minOrderAmount;

    public FlatDiscount() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT discount_amount, min_order_amount FROM discounts WHERE is_active = TRUE LIMIT 1");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                this.discountAmount = rs.getDouble("discount_amount");
                this.minOrderAmount = rs.getDouble("min_order_amount");
            } else {
                this.discountAmount = 0;
                this.minOrderAmount = 500;
            }
        }
    }

    @Override
    public double applyDiscount(double totalAmount) {
        if (totalAmount >= minOrderAmount) {
            return totalAmount - discountAmount;
        }
        return totalAmount;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) throws SQLException {
        if (discountAmount < 0) {
            throw new IllegalArgumentException("Discount cannot be negative!");
        }
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("UPDATE discounts SET discount_amount = ?, min_order_amount = ? WHERE is_active = TRUE")) {
            stmt.setDouble(1, discountAmount);
            stmt.setDouble(2, minOrderAmount);
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                try (PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO discounts (discount_amount, min_order_amount) VALUES (?, ?)")) {
                    insertStmt.setDouble(1, discountAmount);
                    insertStmt.setDouble(2, minOrderAmount);
                    insertStmt.executeUpdate();
                }
            }
            this.discountAmount = discountAmount;
            System.out.println("Discount updated to ₹" + discountAmount);
        }
    }

    public double getMinOrderAmount() {
        return minOrderAmount;
    }

    public void setMinOrderAmount(double minOrderAmount) throws SQLException {
        if (minOrderAmount < 0) {
            throw new IllegalArgumentException("Minimum order amount cannot be negative!");
        }
        this.minOrderAmount = minOrderAmount;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("UPDATE discounts SET min_order_amount = ? WHERE is_active = TRUE")) {
            stmt.setDouble(1, minOrderAmount);
            stmt.executeUpdate();
            System.out.println("Minimum order amount updated to ₹" + minOrderAmount);
        }
    }
}