package com.aurionpro.model;

public class UPI implements PaymentGateway {
    @Override
    public void pay(double amount) {
        System.out.println("Paid ₹" + amount + " via UPI.");
    }

    @Override
    public void refund(double amount) {
        System.out.println("Refunded ₹" + amount + " to UPI account.");
    }
}
