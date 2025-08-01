package com.aurionpro.model;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class TransactionService {
    private final Connection connection;
    private final Scanner scanner = new Scanner(System.in);

    public TransactionService(Connection connection) {
        this.connection = connection;
    }

    public void checkBalance(int userId) {
        try {
            String sql = "SELECT balance FROM users WHERE user_id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("Current Balance: ₹" + rs.getDouble("balance"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching balance: " + e.getMessage());
        }
    }

    public void deposit(int userId) {
        try {
            System.out.print("Enter amount to deposit: ");
            double amount = scanner.nextDouble();
            scanner.nextLine();

            if (amount <= 0) {
                System.out.println("Deposit amount must be greater than 0.");
                return;
            }

            String sql = "UPDATE users SET balance = balance + ? WHERE user_id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setDouble(1, amount);
            ps.setInt(2, userId);
            ps.executeUpdate();

            logTransaction(userId, "DEPOSIT", amount, "Amount deposited");
            System.out.println("Deposit successful.");
        } catch (SQLException e) {
            System.err.println("Deposit error: " + e.getMessage());
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Amount must be numeric.");
            scanner.nextLine();
        }
    }

    public void withdraw(int userId) {
        try {
            System.out.print("Enter amount to withdraw ");
            double amount = scanner.nextDouble();
            scanner.nextLine();

            

            if (!hasSufficientBalance(userId, amount)) {
                System.out.println("Insufficient balance.");
                return;
            }

            String sql = "UPDATE users SET balance = balance - ? WHERE user_id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setDouble(1, amount);
            ps.setInt(2, userId);
            ps.executeUpdate();

            logTransaction(userId, "WITHDRAW", amount, "Amount withdrawn");
            System.out.println("Withdrawal successful.");
        } catch (SQLException e) {
            System.err.println("Withdraw error: " + e.getMessage());
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Amount must be numeric.");
            scanner.nextLine();
        }
    }

    public void transfer(int senderId, String senderName) {
        try {
            System.out.print("Enter recipient username: ");
            String recipient = scanner.nextLine().trim();
            System.out.print("Enter amount to transfer: ");
            double amount = scanner.nextDouble();
            scanner.nextLine();

            int recipientId = getUserId(recipient);

            if (recipientId == -1) {
                System.out.println("Recipient account does not exist.");
                return;
            }

            if (recipientId == senderId) {
                System.out.println("You cannot transfer to your own account.");
                return;
            }

            if (amount <= 0) {
                System.out.println("Amount must be greater than 0.");
                return;
            }

            if (!hasSufficientBalance(senderId, amount)) {
                System.out.println("Insufficient balance.");
                return;
            }

            connection.setAutoCommit(false);

            PreparedStatement debit = connection.prepareStatement("UPDATE users SET balance = balance - ? WHERE user_id = ?");
            debit.setDouble(1, amount);
            debit.setInt(2, senderId);
            debit.executeUpdate();

            PreparedStatement credit = connection.prepareStatement("UPDATE users SET balance = balance + ? WHERE user_id = ?");
            credit.setDouble(1, amount);
            credit.setInt(2, recipientId);
            credit.executeUpdate();

            PreparedStatement transfer = connection.prepareStatement("INSERT INTO money_transfers (sender_id, receiver_id, transfer_amount) VALUES (?, ?, ?)");
            transfer.setInt(1, senderId);
            transfer.setInt(2, recipientId);
            transfer.setDouble(3, amount);
            transfer.executeUpdate();

            logTransaction(senderId, "TRANSFER_OUT", amount, "Transferred to " + recipient);
            logTransaction(recipientId, "TRANSFER_IN", amount, "Received from " + senderName);

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

    public void showTransactions(int userId) {
        try {
            String sql = "SELECT transaction_type, amount, transaction_time, description FROM user_transactions WHERE user_id = ? ORDER BY transaction_time DESC LIMIT 5";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            System.out.println("\nYour Last 5 Transactions:");
            boolean hasTransactions = false;
            while (rs.next()) {
                hasTransactions = true;
                System.out.printf("%-12s ₹%-10.2f [%s] - %s\n",
                        rs.getString("transaction_type"),
                        rs.getDouble("amount"),
                        rs.getTimestamp("transaction_time"),
                        rs.getString("description"));
            }
            if (!hasTransactions) {
                System.out.println("No transactions found.");
            }
        } catch (SQLException e) {
            System.err.println("Transaction history error: " + e.getMessage());
        }
    }

    public void logTransaction(int userId, String type, double amount, String description) throws SQLException {
        String sql = "INSERT INTO user_transactions (user_id, transaction_type, amount, description) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setString(2, type);
        ps.setDouble(3, amount);
        ps.setString(4, description);
        ps.executeUpdate();
    }

    private boolean hasSufficientBalance(int userId, double amount) {
        try {
            String sql = "SELECT balance FROM users WHERE user_id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance") >= amount;
            }
        } catch (SQLException e) {
            System.err.println("Balance check error: " + e.getMessage());
        }
        return false;
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
}
