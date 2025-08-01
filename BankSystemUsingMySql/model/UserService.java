
package com.aurionpro.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class UserService {
    private final Connection connection;
    private final Scanner scanner = new Scanner(System.in);
    private String loggedInUser;
    private int loggedInUserId = -1;
    private final TransactionService transactionService;

    public UserService(Connection connection) {
        this.connection = connection;
        this.transactionService = new TransactionService(connection);
    }

    public void createUser() {
        try {
            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Enter password: ");
            String password = scanner.nextLine().trim();
            System.out.print("Enter initial deposit (min ₹500): ");
            double deposit = scanner.nextDouble();
            scanner.nextLine();

            if (deposit < 500) {
                System.out.println("Initial deposit must be at least ₹500.");
                return;
            }

            String sql = "INSERT INTO users (username, password, balance) VALUES (?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setDouble(3, deposit);
            ps.executeUpdate();

            int userId = getUserId(username);
            transactionService.logTransaction(userId, "DEPOSIT", deposit, "Initial deposit");

            System.out.println("User created successfully.");
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Username already exists.");
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Deposit must be a number.");
            scanner.nextLine();
        }
    }

    public void login() {
        try {
            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Enter password: ");
            String password = scanner.nextLine().trim();

            String sql = "SELECT user_id FROM users WHERE username = ? AND password = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                loggedInUserId = rs.getInt("user_id");
                loggedInUser = username;
                System.out.println("Login successful. Welcome " + capitalize(username));
                userMenu();
            } else {
                System.out.println("Invalid credentials.");
            }
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
        }
    }

    private void userMenu() {
        while (true) {
            System.out.println("\nUSER MENU");
            System.out.println("1. Check Balance");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Transfer");
            System.out.println("5. Change Password");
            System.out.println("6. Show Transactions");
            System.out.println("7. Close Account");
            System.out.println("8. Logout");
            System.out.print("Enter your choice: ");

            try {
                int choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1 -> transactionService.checkBalance(loggedInUserId);
                    case 2 -> transactionService.deposit(loggedInUserId);
                    case 3 -> transactionService.withdraw(loggedInUserId);
                    case 4 -> transactionService.transfer(loggedInUserId, loggedInUser);
                    case 5 -> changePassword();
                    case 6 -> transactionService.showTransactions(loggedInUserId);
                    case 7 -> closeAccount();
                    case 8 -> {
                        loggedInUser = null;
                        loggedInUserId = -1;
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
            }
        }
    }

    private void changePassword() {
        try {
            System.out.print("Enter current password: ");
            String currentPassword = scanner.nextLine().trim();
            System.out.print("Enter new password: ");
            String newPassword = scanner.nextLine().trim();

            String sql = "SELECT password FROM users WHERE user_id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, loggedInUserId);
            ResultSet rs = ps.executeQuery();

            if (rs.next() && rs.getString("password").equals(currentPassword)) {
                sql = "UPDATE users SET password = ? WHERE user_id = ?";
                ps = connection.prepareStatement(sql);
                ps.setString(1, newPassword);
                ps.setInt(2, loggedInUserId);
                ps.executeUpdate();
                System.out.println("Password changed successfully.");
            } else {
                System.out.println("Incorrect current password.");
            }
        } catch (SQLException e) {
            System.err.println("Password update error: " + e.getMessage());
        }
    }

    private void closeAccount() {
        try {
            String sql = "DELETE FROM users WHERE user_id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, loggedInUserId);
            ps.executeUpdate();
            loggedInUser = null;
            loggedInUserId = -1;
            System.out.println("Account closed successfully.");
        } catch (SQLException e) {
            System.err.println("Error closing account: " + e.getMessage());
        }
    }

    private int getUserId(String username) {
        try {
            String sql = "SELECT user_id FROM users WHERE username = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("user_id");
        } catch (SQLException e) {
            System.err.println("Error fetching user ID: " + e.getMessage());
        }
        return -1;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
