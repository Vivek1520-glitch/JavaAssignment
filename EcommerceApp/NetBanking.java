package com.aurionpro.model;

public class NetBanking implements PaymentGateway {
    @Override
    public void pay(double amount) {
        System.out.println("Paid " + amount + " using NetBanking.");
    }

    @Override
    public void refund(double amount) {
        System.out.println("Refunded " + amount + " via NetBanking.");
    }
}
