package com.aurionpro.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DeliveryPartnerManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/food_ordering_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1520";

    public void addPartner(String name) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Partner name cannot be empty!");
        }
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO delivery_partners (partner_name) VALUES (?)")) {
            stmt.setString(1, name);
            stmt.executeUpdate();
            System.out.println("Delivery partner added: " + name);
        }
    }

    public void removePartner(int index) throws SQLException {
        List<Integer> partnerIds = getPartnerIds();
        if (index >= 0 && index < partnerIds.size()) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement("UPDATE delivery_partners SET is_active = FALSE WHERE partner_id = ?")) {
                stmt.setInt(1, partnerIds.get(index));
                stmt.executeUpdate();
                System.out.println("Removed partner at index: " + index);
            }
        } else {
            System.out.println("Invalid choice!");
        }
    }

    public void displayPartners() throws SQLException {
        List<String> partners = new ArrayList<>();
        List<Integer> partnerIds = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT partner_id, partner_name FROM delivery_partners WHERE is_active = TRUE");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                partnerIds.add(rs.getInt("partner_id"));
                partners.add(rs.getString("partner_name"));
            }
        }
        System.out.println("\n--- Delivery Partners ---");
        for (int i = 0; i < partners.size(); i++) {
            System.out.println((i + 1) + ". " + partners.get(i));
        }
        System.out.println("---------------------------");
    }

    public String getRandomPartner() throws SQLException {
        List<String> partners = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT partner_name FROM delivery_partners WHERE is_active = TRUE");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                partners.add(rs.getString("partner_name"));
            }
        }
        if (partners.isEmpty()) {
            throw new IllegalStateException("No active delivery partners available!");
        }
        Random rand = new Random();
        return partners.get(rand.nextInt(partners.size()));
    }

    private List<Integer> getPartnerIds() throws SQLException {
        List<Integer> partnerIds = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT partner_id FROM delivery_partners WHERE is_active = TRUE");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                partnerIds.add(rs.getInt("partner_id"));
            }
        }
        return partnerIds;
    }
}