package com.aurionpro.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Menu {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/food_ordering_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1520";
    private Map<String, List<MenuItem>> cuisines;

    public Menu() throws SQLException {
        cuisines = new LinkedHashMap<>();
        loadCuisines();
    }

    private void loadCuisines() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT cuisine_id, cuisine_name FROM cuisines");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String cuisineName = rs.getString("cuisine_name");
                int cuisineId = rs.getInt("cuisine_id");
                List<MenuItem> items = loadItems(cuisineId);
                cuisines.put(cuisineName, items);
            }
        }
    }

    private List<MenuItem> loadItems(int cuisineId) throws SQLException {
        List<MenuItem> items = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT item_id, item_name, price, stock_quantity FROM menu_items WHERE cuisine_id = ?");
             ) {
            stmt.setInt(1, cuisineId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new MenuItem(
                            rs.getInt("item_id"),
                            rs.getString("item_name"),
                            rs.getDouble("price"),
                            rs.getInt("stock_quantity")
                    ));
                }
            }
        }
        return items;
    }

    public void displayMenu() {
        System.out.println("\n===== MENU =====");
        int cuisineIndex = 1;
        for (String cuisine : cuisines.keySet()) {
            System.out.println(cuisineIndex + ". " + cuisine);
            cuisineIndex++;
        }
        System.out.println(cuisineIndex + ". Exit");
    }

    public void displayCuisineItems(String cuisine) throws SQLException {
        if (!cuisines.containsKey(cuisine)) {
            System.out.println("Invalid cuisine!");
            return;
        }
        List<MenuItem> items = cuisines.get(cuisine);
        System.out.println("\n--- " + cuisine + " Menu ---");
        for (int i = 0; i < items.size(); i++) {
            System.out.println((i + 1) + ". " + items.get(i).getName() + " - ₹" + items.get(i).getPrice() +
                    " (Stock: " + items.get(i).getStockQuantity() + ")");
        }
    }

    public MenuItem getItem(String cuisine, int index) {
        if (cuisines.containsKey(cuisine) && index >= 0 && index < cuisines.get(cuisine).size()) {
            return cuisines.get(cuisine).get(index);
        }
        return null;
    }

    public Set<String> getCuisines() {
        return cuisines.keySet();
    }

    public void addCuisine(String cuisineName) throws SQLException {
        if (cuisineName == null || cuisineName.trim().isEmpty()) {
            throw new IllegalArgumentException("Cuisine name cannot be empty!");
        }
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO cuisines (cuisine_name) VALUES (?)")) {
            stmt.setString(1, cuisineName);
            stmt.executeUpdate();
            cuisines.put(cuisineName, new ArrayList<>());
            System.out.println("Cuisine added: " + cuisineName);
        }
    }

    public void addItem(String cuisine, String itemName, double price, int stock) throws SQLException {
        if (!cuisines.containsKey(cuisine)) {
            throw new IllegalArgumentException("Cuisine not found!");
        }
        if (itemName == null || itemName.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be empty!");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive!");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative!");
        }
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO menu_items (cuisine_id, item_name, price, stock_quantity) VALUES ((SELECT cuisine_id FROM cuisines WHERE cuisine_name = ?), ?, ?, ?)")) {
            stmt.setString(1, cuisine);
            stmt.setString(2, itemName);
            stmt.setDouble(3, price);
            stmt.setInt(4, stock);
            stmt.executeUpdate();
            cuisines.get(cuisine).add(new MenuItem(itemName, price, stock));
            System.out.println("Item added successfully!");
        }
    }

    public void removeItem(String cuisine, int index) throws SQLException {
        if (cuisines.containsKey(cuisine) && index >= 0 && index < cuisines.get(cuisine).size()) {
            MenuItem item = cuisines.get(cuisine).get(index);
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM menu_items WHERE item_id = ?")) {
                stmt.setInt(1, item.getItemId());
                stmt.executeUpdate();
                cuisines.get(cuisine).remove(index);
                System.out.println("Item removed successfully!");
            }
        } else {
            System.out.println("Invalid choice!");
        }
    }

    public boolean checkStock(String cuisine, int index, int quantity) throws SQLException {
        if (cuisines.containsKey(cuisine) && index >= 0 && index < cuisines.get(cuisine).size()) {
            MenuItem item = cuisines.get(cuisine).get(index);
            return item.getStockQuantity() >= quantity;
        }
        return false;
    }

    public void reduceStock(String cuisine, int index, int quantity) throws SQLException {
        if (cuisines.containsKey(cuisine) && index >= 0 && index < cuisines.get(cuisine).size()) {
            MenuItem item = cuisines.get(cuisine).get(index);
            int newStock = item.getStockQuantity() - quantity;
            if (newStock < 0) {
                throw new IllegalStateException("Insufficient stock!");
            }
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement("UPDATE menu_items SET stock_quantity = ? WHERE item_id = ?")) {
                stmt.setInt(1, newStock);
                stmt.setInt(2, item.getItemId());
                stmt.executeUpdate();
                item.setStockQuantity(newStock);
            }
        }
    }

    public void updateItemStock(String cuisine, int index, int stock) throws SQLException {
        if (cuisines.containsKey(cuisine) && index >= 0 && index < cuisines.get(cuisine).size()) {
            if (stock < 0) {
                throw new IllegalArgumentException("Stock quantity cannot be negative!");
            }
            MenuItem item = cuisines.get(cuisine).get(index);
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement("UPDATE menu_items SET stock_quantity = ? WHERE item_id = ?")) {
                stmt.setInt(1, stock);
                stmt.setInt(2, item.getItemId());
                stmt.executeUpdate();
                item.setStockQuantity(stock);
                System.out.println("Stock updated for " + item.getName() + " to " + stock);
            }
        } else {
            System.out.println("Invalid choice!");
        }
    }
}