package com.aurionpro.test;

import java.util.Scanner;

import com.aurionpro.model.CreditCard;
import com.aurionpro.model.NetBanking;
import com.aurionpro.model.PaymentGateway;
import com.aurionpro.model.UPI;

public class CheckOut {

    public void run() {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            try {
                System.out.println("\nE-Commerce Payment System ");
                System.out.println("Choose Payment Method:");
                System.out.println("1. Credit Card");
                System.out.println("2. UPI");
                System.out.println("3. NetBanking");
                System.out.println("4. Exit");
                System.out.print("Enter your choice: ");

                if (!scanner.hasNextInt()) {
                    throw new IllegalArgumentException("Invalid input! Please enter a number from 1 to 4.");
                }

                int choice = scanner.nextInt();
                scanner.nextLine();

                if (choice == 4) {
                    System.out.println("Thank you for shopping with us!");
                    break;
                }

                PaymentGateway gateway = null;

                switch (choice) {
                    case 1:
                        gateway = new CreditCard();
                        break;
                    case 2:
                        gateway = new UPI();
                        break;
                    case 3:
                        gateway = new NetBanking();
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid selection! Please choose between 1 to 4.");
                }

                System.out.print("Enter amount: ");
                if (!scanner.hasNextDouble()) {
                    throw new IllegalArgumentException("Amount must be a number.");
                }

                double amount = scanner.nextDouble();
                scanner.nextLine();

                System.out.println("1. Pay");
                System.out.println("2. Refund");
                System.out.print("Choose transaction type: ");

                if (!scanner.hasNextInt()) {
                    throw new IllegalArgumentException("Invalid transaction type!");
                }

                int trans = scanner.nextInt();
                scanner.nextLine();

                if (trans == 1) {
                    gateway.pay(amount);
                } else if (trans == 2) {
                    gateway.refund(amount);
                } else {
                    System.out.println("Invalid transaction type.");
                }

            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
                scanner.nextLine(); 
            } catch (Exception e) {
                System.out.println("Unexpected Error: " + e.getMessage());
                scanner.nextLine();
            }
        }

        scanner.close();
    }

    public static void main(String[] args) {
        CheckOut checkout = new CheckOut();
        checkout.run();
    }
}
