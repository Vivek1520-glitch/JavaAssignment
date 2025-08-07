package com.aurionpro.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Order {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/food_ordering_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1520";
    private Map<MenuItem, Integer> cart;
    private double totalAmount;

    public Order() {
        cart = new LinkedHashMap<>();
        totalAmount = 0;
    }

    public void addItem(MenuItem item, int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be positive!");
        }
        cart.put(item, cart.getOrDefault(item, 0) + qty);
        recalculateTotal();
        System.out.println(item.getName() + " added to cart.");
    }

    public void removeItem(int index) {
        if (index < 0 || index >= cart.size()) {
            System.out.println("Invalid item number!");
            return;
        }
        MenuItem item = new ArrayList<>(cart.keySet()).get(index);
        int quantity = cart.get(item);
        if (quantity > 1) {
            cart.put(item, quantity - 1);
            System.out.println("Removed one quantity of " + item.getName() + ". Remaining: " + (quantity - 1));
        } else {
            cart.remove(item);
            System.out.println(item.getName() + " removed from the cart.");
        }
        recalculateTotal();
    }

    public void viewCart() {
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty!");
            return;
        }
        System.out.println("\n--- Your Cart ---");
        int index = 1;
        for (Map.Entry<MenuItem, Integer> entry : cart.entrySet()) {
            System.out.println(index + ". " + entry.getKey().getName() + " x " + entry.getValue() +
                    " = ₹" + (entry.getKey().getPrice() * entry.getValue()));
            index++;
        }
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public boolean isEmpty() {
        return cart.isEmpty();
    }

    public Map<MenuItem, Integer> getItems() {
        return cart;
    }

    public int saveOrder(double discountedTotal, PaymentMode paymentMode, String deliveryPartner) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            conn.setAutoCommit(false);
            try {
                // Insert order
                int orderId;
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO orders (total_amount, discounted_amount, payment_mode, delivery_partner_id, order_status) " +
                                "VALUES (?, ?, ?, (SELECT partner_id FROM delivery_partners WHERE partner_name = ? AND is_active = TRUE), ?)",
                        PreparedStatement.RETURN_GENERATED_KEYS)) {
                    stmt.setDouble(1, totalAmount);
                    stmt.setDouble(2, discountedTotal);
                    stmt.setString(3, paymentMode.name());
                    stmt.setString(4, deliveryPartner);
                    stmt.setString(5, "CONFIRMED");
                    stmt.executeUpdate();
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        rs.next();
                        orderId = rs.getInt(1);
                    }
                }

                // Insert order items
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO order_items (order_id, item_id, quantity) VALUES (?, ?, ?)")) {
                    for (Map.Entry<MenuItem, Integer> entry : cart.entrySet()) {
                        stmt.setInt(1, orderId);
                        stmt.setInt(2, entry.getKey().getItemId());
                        stmt.setInt(3, entry.getValue());
                        stmt.executeUpdate();
                    }
                }

                conn.commit();
                return orderId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private void recalculateTotal() {
        totalAmount = 0;
        for (Map.Entry<MenuItem, Integer> entry : cart.entrySet()) {
            totalAmount += entry.getKey().getPrice() * entry.getValue();
        }
    }
}