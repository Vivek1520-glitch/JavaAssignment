package com.aurionpro.model;

public class CreditCard implements PaymentGateway {
    private double balance = 50000;

    public void pay(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            System.out.println("Paid Rs." + amount + " using Credit Card.");
            System.out.println("Remaining Credit Card balance: Rs." + balance);
        } else {
            System.out.println("Payment failed: Invalid or insufficient Credit Card balance.");
        }
    }

    public void refund(double amount) {
        if (amount > 0) {
            balance += amount;
            System.out.println("Refunded Rs." + amount + " to Credit Card.");
            System.out.println("Updated Credit Card balance: Rs." + balance);
        } else {
            System.out.println("Invalid refund amount for Credit Card.");
        }
    }
}