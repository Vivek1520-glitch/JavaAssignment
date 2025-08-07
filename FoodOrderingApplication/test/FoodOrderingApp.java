package com.aurionpro.test;

import com.aurionpro.model.DeliveryPartnerManager;
import com.aurionpro.model.FoodOrderingSystemFacade;
import com.aurionpro.model.Menu;
import com.aurionpro.model.Discount;
import com.aurionpro.model.FlatDiscount;

import java.sql.SQLException;
import java.util.Scanner;

public class FoodOrderingApp {
    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            Menu menu = new Menu();
            Discount discount = new FlatDiscount();
            DeliveryPartnerManager deliveryManager = new DeliveryPartnerManager();
            FoodOrderingSystemFacade system = new FoodOrderingSystemFacade(menu, discount, deliveryManager);

            while (true) {
                System.out.println("\n=== Welcome to Food Ordering App ===");
                System.out.println("1. Admin Login");
                System.out.println("2. Customer");
                System.out.println("3. Exit");
                System.out.print("Enter choice: ");
                int choice = system.getIntInput(sc);

                switch (choice) {
                    case 1 -> {
                        System.out.print("Enter Admin Password: ");
                        sc.nextLine(); 
                        String pass = sc.nextLine();
                        if (pass.equals("admin123")) {
                            boolean adminLoop = true;
                            while (adminLoop) {
                                System.out.println("\n--- Admin Menu ---");
                                System.out.println("1. Manage Menu");
                                System.out.println("2. Manage Discounts");
                                System.out.println("3. Manage Delivery Partners");
                                System.out.println("4. View Order History");
                                System.out.println("5. Back");
                                System.out.print("Enter choice: ");
                                int adminChoice = system.getIntInput(sc);

                                switch (adminChoice) {
                                case 1 -> system.manageMenu(sc);
                                case 2 -> system.manageDiscount(sc);
                                case 3 -> system.manageDeliveryPartners(sc);
                                case 4 -> system.viewOrderHistory();
                                case 5 -> {
                                        System.out.println("Returning to main menu...");
                                        adminLoop = false;
                                    }
                                    default -> System.out.println("Invalid choice!");
                                }
                            }
                        } else {
                            System.out.println("Invalid password!");
                        }
                    }
                    case 2 -> system.customerOperations(sc);
                    case 3 -> {
                        System.out.println("Exiting...");
                        return;
                    }
                    default -> System.out.println("Invalid choice!");
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to initialize application: " + e.getMessage());
        }
    }
}