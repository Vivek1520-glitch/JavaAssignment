package com.aurionpro.model;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;

public class FoodOrderingSystemFacade {
    private Menu menu;
    private Discount discount;
    private DeliveryPartnerManager deliveryManager;

    public FoodOrderingSystemFacade(Menu menu, Discount discount, DeliveryPartnerManager deliveryManager) {
        this.menu = menu;
        this.discount = discount;
        this.deliveryManager = deliveryManager;
    }

    public void adminOperations(Scanner sc) {
        while (true) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. Manage Menu");
            System.out.println("2. Manage Discount");
            System.out.println("3. Manage Delivery Partners");
            System.out.println("4. Back");
            System.out.print("Enter choice: ");

            int choice = getIntInput(sc);
            switch (choice) {
                case 1 -> manageMenu(sc);
                case 2 -> manageDiscount(sc);
                case 3 -> manageDeliveryPartners(sc);
                case 4 -> { return; }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private void manageMenu(Scanner sc) {
        while (true) {
            try {
                System.out.println("\n--- Manage Menu ---");
                int i = 1;
                for (String cuisine : menu.getCuisines()) {
                    System.out.println(i++ + ". " + cuisine);
                }
                System.out.println(i + ". Add New Cuisine");
                System.out.println((i + 1) + ". Back");
                System.out.print("Select option: ");
                int choice = getIntInput(sc);

                if (choice == i) {
                    System.out.print("Enter new cuisine name: ");
                    sc.nextLine();
                    String cuisineName = sc.nextLine().trim();
                    if (cuisineName.isEmpty()) {
                        System.out.println("Cuisine name cannot be empty!");
                        continue;
                    }
                    menu.addCuisine(cuisineName);
                    continue;
                }
                if (choice == i + 1) return;

                String cuisine = new ArrayList<>(menu.getCuisines()).get(choice - 1);
                System.out.println("1. Add Item");
                System.out.println("2. Remove Item");
                System.out.println("3. Update Item Stock");
                System.out.println("4. Back");
                int subChoice = getIntInput(sc);

                switch (subChoice) {
                    case 1 -> {
                        System.out.print("Enter item name: ");
                        sc.nextLine();
                        String itemName = sc.nextLine().trim();
                        if (itemName.isEmpty()) {
                            System.out.println("Item name cannot be empty!");
                            continue;
                        }
                        System.out.print("Enter price: ");
                        double price = getDoubleInput(sc);
                        System.out.print("Enter stock quantity: ");
                        int stock = getIntInput(sc);
                        menu.addItem(cuisine, itemName, price, stock);
                    }
                    case 2 -> {
                        menu.displayCuisineItems(cuisine);
                        System.out.print("Enter item number to remove: ");
                        int index = getIntInput(sc) - 1;
                        menu.removeItem(cuisine, index);
                    }
                    case 3 -> {
                        menu.displayCuisineItems(cuisine);
                        System.out.print("Enter item number to update stock: ");
                        int index = getIntInput(sc) - 1;
                        System.out.print("Enter new stock quantity: ");
                        int stock = getIntInput(sc);
                        menu.updateItemStock(cuisine, index, stock);
                    }
                    case 4 -> { return; }
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private void manageDiscount(Scanner sc) {
        try {
            System.out.println("Current Discount: ₹" + ((FlatDiscount) discount).getDiscountAmount() +
                    " (Min Order: ₹" + ((FlatDiscount) discount).getMinOrderAmount() + ")");
            System.out.print("Enter new discount amount: ");
            double amount = getDoubleInput(sc);
            System.out.print("Enter minimum order amount for discount: ");
            double minOrder = getDoubleInput(sc);
            ((FlatDiscount) discount).setDiscountAmount(amount);
            ((FlatDiscount) discount).setMinOrderAmount(minOrder);
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private void manageDeliveryPartners(Scanner sc) {
        while (true) {
            try {
                System.out.println("\n--- Delivery Partner Management ---");
                deliveryManager.displayPartners();
                System.out.println("1. Add Partner");
                System.out.println("2. Remove Partner");
                System.out.println("3. Back");
                int choice = getIntInput(sc);

                switch (choice) {
                    case 1 -> {
                        System.out.print("Enter partner name: ");
                        sc.nextLine();
                        String name = sc.nextLine().trim();
                        deliveryManager.addPartner(name);
                    }
                    case 2 -> {
                        System.out.print("Enter index to remove: ");
                        int index = getIntInput(sc) - 1;
                        deliveryManager.removePartner(index);
                    }
                    case 3 -> { return; }
                    default -> System.out.println("Invalid choice!");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    public void customerOperations(Scanner sc) {
        Order order = new Order();
        while (true) {
            try {
                System.out.println("\n--- Customer Menu ---");
                System.out.println("1. View & Add Items");
                System.out.println("2. View Cart");
                System.out.println("3. Remove Item from Cart");
                System.out.println("4. Proceed to Payment");
                System.out.println("5. Back");
                int choice = getIntInput(sc);

                switch (choice) {
                    case 1 -> navigateMenu(sc, order);
                    case 2 -> order.viewCart();
                    case 3 -> {
                        order.viewCart();
                        if (!order.isEmpty()) {
                            System.out.print("Enter item number to remove: ");
                            int index = getIntInput(sc) - 1;
                            order.removeItem(index);
                        }
                    }
                    case 4 -> {
                        if (order.isEmpty()) {
                            System.out.println("Cart is empty!");
                            continue;
                        }
                        proceedToPayment(sc, order);
                        return;
                    }
                    case 5 -> { return; }
                    default -> System.out.println("Invalid choice!");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private void navigateMenu(Scanner sc, Order order) {
        while (true) {
            try {
                menu.displayMenu();
                System.out.print("Select cuisine: ");
                int choice = getIntInput(sc);
                if (choice == menu.getCuisines().size() + 1) return;

                String cuisine = new ArrayList<>(menu.getCuisines()).get(choice - 1);
                menu.displayCuisineItems(cuisine);
                System.out.print("Enter item number to add (0 to go back): ");
                int itemChoice = getIntInput(sc);
                if (itemChoice == 0) continue;

                MenuItem item = menu.getItem(cuisine, itemChoice - 1);
                if (item != null) {
                    System.out.print("Enter quantity: ");
                    int qty = getIntInput(sc);
                    if (menu.checkStock(cuisine, itemChoice - 1, qty)) {
                        order.addItem(item, qty);
                        menu.reduceStock(cuisine, itemChoice - 1, qty);
                    } else {
                        System.out.println("Insufficient stock for " + item.getName() + "!");
                    }
                } else {
                    System.out.println("Invalid item!");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private void proceedToPayment(Scanner sc, Order order) throws SQLException {
        double total = order.getTotalAmount();
        double discountedTotal = discount.applyDiscount(total);
        System.out.println("\nTotal: ₹" + total);
        if (discountedTotal < total) {
            System.out.println("Discount Applied: ₹" + (total - discountedTotal));
        }
        System.out.println("Final Amount: ₹" + discountedTotal);

        PaymentMode paymentMode = null;
        while (paymentMode == null) {
            System.out.print("Choose Payment Mode (1. CASH  2. UPI): ");
            int payChoice = getIntInput(sc);
            if (payChoice == 1) paymentMode = PaymentMode.CASH;
            else if (payChoice == 2) paymentMode = PaymentMode.UPI;
            else System.out.println("Invalid choice!");
        }

        String partner = deliveryManager.getRandomPartner();
        int orderId = order.saveOrder(discountedTotal, paymentMode, partner);
        System.out.println("\nOrder Confirmed! Order ID: " + orderId);
        System.out.println("Payment Mode: " + paymentMode);
        System.out.println("Delivery Partner: " + partner);
        System.out.println("Thank you for ordering!");

        // Generate and display invoice
        generateInvoice(order, orderId, total, discountedTotal, paymentMode, partner);
    }

    private void generateInvoice(Order order, int orderId, double total, double discountedTotal, PaymentMode paymentMode, String deliveryPartner) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        System.out.println("\n=====================================");
        System.out.println("           ORDER INVOICE             ");
        System.out.println("=====================================");
        System.out.println("Order ID: " + orderId);
        System.out.println("Date & Time: " + now.format(formatter));
        System.out.println("-------------------------------------");
        System.out.println("Items Ordered:");
        int index = 1;
        for (Map.Entry<MenuItem, Integer> entry : order.getItems().entrySet()) {
            MenuItem item = entry.getKey();
            int quantity = entry.getValue();
            System.out.printf("%d. %s x %d = ₹%.2f%n", 
                index++, item.getName(), quantity, item.getPrice() * quantity);
        }
        System.out.println("-------------------------------------");
        System.out.printf("Total Amount: ₹%.2f%n", total);
        if (total > discountedTotal) {
            System.out.printf("Discount Applied: ₹%.2f%n", total - discountedTotal);
        }
        System.out.printf("Final Amount: ₹%.2f%n", discountedTotal);
        System.out.println("Payment Mode: " + paymentMode);
        System.out.println("Delivery Partner: " + deliveryPartner);
        System.out.println("-------------------------------------");
        System.out.println("Thank you for your order!");
        System.out.println("=====================================");
    }

    public int getIntInput(Scanner sc) {
        while (true) {
            try {
                return sc.nextInt();
            } catch (InputMismatchException e) {
                System.out.print("Invalid input! Enter a number: ");
                sc.nextLine();
            }
        }
    }

    private double getDoubleInput(Scanner sc) {
        while (true) {
            try {
                return sc.nextDouble();
            } catch (InputMismatchException e) {
                System.out.print("Invalid input! Enter a valid number: ");
                sc.nextLine();
            }
        }
    }
}