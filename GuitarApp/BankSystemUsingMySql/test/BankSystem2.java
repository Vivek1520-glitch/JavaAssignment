package com.aurionpro.test;

import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class BankSystem2 {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/real_banking_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1520";

    private static Connection connection;
    private static Scanner scanner = new Scanner(System.in);
    private static String loggedInUser = null;
    private static int loggedInUserId = -1;

    public static void main(String[] args) {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Connected to Database!");

            while (true) {
                System.out.println("\nBANK SYSTEM MENU");
                System.out.println("1. Create Account");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.print("Enter your choice: ");

                try {
                    int choice = scanner.nextInt();
                    scanner.nextLine();
                    switch (choice) {
                        case 1 -> createAccount();
                        case 2 -> login();
                        case 3 -> {
                            System.out.println("Exiting...");
                            return;
                        }
                        default -> System.out.println("Invalid choice!");
                    }
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input. Please enter a number.");
                    scanner.nextLine();
                }
            }
        } catch (SQLException e) {
            System.err.println("Database Error: " + e.getMessage());
        }
    }

    private static void createAccount() {
        try {
            System.out.print("Enter username: ");
            String name = scanner.nextLine().trim();
            System.out.print("Enter password: ");
            String password = scanner.nextLine().trim();
            System.out.print("Enter initial deposit (min ₹500): ");
            double deposit = scanner.nextDouble();
            scanner.nextLine();

            if (deposit < 500) {
                System.out.println("Initial deposit must be at least ₹500.");
                return;
            }

            String sql = "INSERT INTO accounts (name, password, balance) VALUES (?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, password);
            ps.setDouble(3, deposit);
            ps.executeUpdate();

            int accountId = getAccountId(name);
            logTransaction(accountId, "DEPOSIT", deposit, "Initial deposit");

            System.out.println("Account created successfully.");
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Username already exists.");
        } catch (SQLException e) {
            System.err.println("Error creating account: " + e.getMessage());
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Deposit must be a number.");
            scanner.nextLine();
        }
    }

    private static void login() {
        try {
            System.out.print("Enter username: ");
            String name = scanner.nextLine().trim();
            System.out.print("Enter password: ");
            String password = scanner.nextLine().trim();

            String sql = "SELECT id FROM accounts WHERE name = ? AND password = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                loggedInUserId = rs.getInt("id");
                loggedInUser = name;
                System.out.println("Login successful. Welcome " + capitalize(name));
                accountMenu();
            } else {
                System.out.println("Invalid credentials.");
            }
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
        }
    }

    private static void accountMenu() {
        while (true) {
            System.out.println("\nACCOUNT MENU");
            System.out.println("1. Check Balance");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Transfer");
            System.out.println("5. Change Password");
            System.out.println("6. Show Transactions");
            System.out.println("7. Logout");
            System.out.print("Enter your choice: ");

            try {
                int choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1 -> checkBalance();
                    case 2 -> deposit();
                    case 3 -> withdraw();
                    case 4 -> transfer();
                    case 5 -> changePassword();
                    case 6 -> showTransactions();
                    case 7 -> {
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

    private static void checkBalance() {
        try {
            String sql = "SELECT balance FROM accounts WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, loggedInUserId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("Current Balance: ₹" + rs.getDouble("balance"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching balance: " + e.getMessage());
        }
    }

    private static void deposit() {
        try {
            System.out.print("Enter amount to deposit: ");
            double amount = scanner.nextDouble();
            scanner.nextLine();

            if (amount <= 0) {
                System.out.println("Deposit amount must be greater than 0.");
                return;
            }

            String sql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setDouble(1, amount);
            ps.setInt(2, loggedInUserId);
            ps.executeUpdate();

            logTransaction(loggedInUserId, "DEPOSIT", amount, "Amount deposited");

            System.out.println("Deposit successful.");
        } catch (SQLException e) {
            System.err.println("Deposit error: " + e.getMessage());
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Amount must be numeric.");
            scanner.nextLine();
        }
    }

    private static void withdraw() {
        try {
            System.out.print("Enter amount to withdraw (₹100 or ₹500 multiples): ");
            double amount = scanner.nextDouble();
            scanner.nextLine();

            if (amount <= 0 || (amount % 100 != 0 && amount % 500 != 0)) {
                System.out.println("Withdrawals must be in multiples of ₹100 or ₹500.");
                return;
            }

            if (!hasSufficientBalance(amount)) {
                System.out.println("Insufficient balance.");
                return;
            }

            String sql = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setDouble(1, amount);
            ps.setInt(2, loggedInUserId);
            ps.executeUpdate();

            logTransaction(loggedInUserId, "WITHDRAW", amount, "Amount withdrawn");

            System.out.println("Withdrawal successful.");
        } catch (SQLException e) {
            System.err.println("Withdraw error: " + e.getMessage());
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Amount must be numeric.");
            scanner.nextLine();
        }
    }

    private static void transfer() {
        try {
            System.out.print("Enter recipient username: ");
            String recipient = scanner.nextLine().trim();
            System.out.print("Enter amount to transfer: ");
            double amount = scanner.nextDouble();
            scanner.nextLine();

            int recipientId = getAccountId(recipient);

            if (recipientId == -1) {
                System.out.println("Recipient account does not exist.");
                return;
            }

            if (recipientId == loggedInUserId) {
                System.out.println("You cannot transfer to your own account.");
                return;
            }

            if (amount <= 0) {
                System.out.println("Amount must be greater than 0.");
                return;
            }

            if (!hasSufficientBalance(amount)) {
                System.out.println("Insufficient balance.");
                return;
            }

            connection.setAutoCommit(false);

            PreparedStatement debit = connection.prepareStatement("UPDATE accounts SET balance = balance - ? WHERE id = ?");
            debit.setDouble(1, amount);
            debit.setInt(2, loggedInUserId);
            debit.executeUpdate();

            PreparedStatement credit = connection.prepareStatement("UPDATE accounts SET balance = balance + ? WHERE id = ?");
            credit.setDouble(1, amount);
            credit.setInt(2, recipientId);
            credit.executeUpdate();

            PreparedStatement transfer = connection.prepareStatement("INSERT INTO transfers (sender_id, receiver_id, amount) VALUES (?, ?, ?)");
            transfer.setInt(1, loggedInUserId);
            transfer.setInt(2, recipientId);
            transfer.setDouble(3, amount);
            transfer.executeUpdate();

            logTransaction(loggedInUserId, "TRANSFER_OUT", amount, "Transferred to " + recipient);
            logTransaction(recipientId, "TRANSFER_IN", amount, "Received from " + loggedInUser);

            connection.commit();
            connection.setAutoCommit(true);

            System.out.println("Transfer successful.");
        } catch (SQLException e) {
            try {
                connection.rollback();
                System.out.println("Transfer failed. Transaction rolled back.");
            } catch (SQLException ex) {
                System.err.println("Rollback failed: " + ex.getMessage());
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Amount must be numeric.");
            scanner.nextLine();
        }
    }

    private static void changePassword() {
        try {
            System.out.print("Enter current password: ");
            String currentPassword = scanner.nextLine().trim();
            System.out.print("Enter new password: ");
            String newPassword = scanner.nextLine().trim();

            String sql = "SELECT password FROM accounts WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, loggedInUserId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getString("password").equals(currentPassword)) {
                sql = "UPDATE accounts SET password = ? WHERE id = ?";
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

    private static void showTransactions() {
        try {
            String sql = "SELECT type, amount, timestamp, description FROM transactions WHERE account_id = ? ORDER BY timestamp DESC";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, loggedInUserId);
            ResultSet rs = ps.executeQuery();

            System.out.println("\nYour Transactions:");
            boolean hasTransactions = false;
            while (rs.next()) {
                hasTransactions = true;
                System.out.printf("%-12s ₹%-10.2f [%s] - %s\n",
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getTimestamp("timestamp"),
                        rs.getString("description"));
            }
            if (!hasTransactions) {
                System.out.println("No transactions found.");
            }
        } catch (SQLException e) {
            System.err.println("Transaction history error: " + e.getMessage());
        }
    }

    private static void logTransaction(int accountId, String type, double amount, String description) throws SQLException {
        String sql = "INSERT INTO transactions (account_id, type, amount, description) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, accountId);
        ps.setString(2, type);
        ps.setDouble(3, amount);
        ps.setString(4, description);
        ps.executeUpdate();
    }

    private static int getAccountId(String username) {
        try {
            String sql = "SELECT id FROM accounts WHERE name = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            System.err.println("Error fetching account ID: " + e.getMessage());
        }
        return -1;
    }

    private static boolean hasSufficientBalance(double amount) {
        try {
            String sql = "SELECT balance FROM accounts WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, loggedInUserId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance") >= amount;
            }
        } catch (SQLException e) {
            System.err.println("Balance check error: " + e.getMessage());
        }
        return false;
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}