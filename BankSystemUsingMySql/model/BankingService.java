package com.aurionpro.model;



import java.sql.Connection;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;


public class BankingService {
 private final Scanner scanner = new Scanner(System.in);
 private Connection connection;
 private final UserService userService;

 public BankingService() {
     try {
         connection = DatabaseUtil.getConnection();
         userService = new UserService(connection);
     } catch (SQLException e) {
         throw new RuntimeException("Failed to connect to database: " + e.getMessage());
     }
 }

 public void start() {
     System.out.println("Connected to Database!");

     while (true) {
         System.out.println("\nBANK SYSTEM MENU");
         System.out.println("1. Create User");
         System.out.println("2. Login");
         System.out.println("3. Exit");
         System.out.print("Enter your choice: ");

         try {
             int choice = scanner.nextInt();
             scanner.nextLine();
             switch (choice) {
                 case 1 -> userService.createUser();
                 case 2 -> userService.login();
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
 }
}

