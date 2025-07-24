package com.aurionpro.test;

import com.aurionpro.model.*;

import java.util.*;

public class FindGuitarTester {
    public static void main(String[] args) {
        Inventory inventory = new Inventory();
        initializeInventory(inventory);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nGuitar Inventory System");
            System.out.println("1. Admin");
            System.out.println("2. User");
            System.out.println("3. Exit");
            System.out.print("Choose your role: ");
            int choice = readInt(scanner);

            switch (choice) {
                case 1:
                    runAdminModule(scanner, inventory);
                    break;
                case 2:
                    runUserModule(scanner, inventory);
                    break;
                case 3:
                    System.out.println("Exiting application. Thank you!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void runAdminModule(Scanner scanner, Inventory inventory) {
        while (true) {
            System.out.println("\n Admin Menu ");
            System.out.println("1. Add Guitar");
            System.out.println("2. Display All Guitars");
            System.out.println("3. Exit Admin Module");
            System.out.print("Select option: ");
            int choice = readInt(scanner);

            switch (choice) {
                case 1:
                    System.out.print("Enter Serial Number: ");
                    String serialNumber = scanner.nextLine();
                    System.out.print("Enter Price: ");
                    double price = readDouble(scanner);

                    showOptions("Select Builder", Builder.values());
                    Builder builder = Builder.values()[readInt(scanner) - 1];

                    System.out.print("Enter Model Name: ");
                    String model = scanner.nextLine();

                    showOptions("Select Type", Type.values());
                    Type type = Type.values()[readInt(scanner) - 1];

                    System.out.print("Enter Number of Strings: ");
                    int numStrings = readInt(scanner);

                    showOptions("Select Back Wood", Wood.values());
                    Wood backWood = Wood.values()[readInt(scanner) - 1];

                    showOptions("Select Top Wood", Wood.values());
                    Wood topWood = Wood.values()[readInt(scanner) - 1];

                    inventory.addGuitar(serialNumber, price, builder, model, type, numStrings, backWood, topWood);
                    System.out.println("Guitar added successfully.");
                    break;

                case 2:
                    showAllGuitars(inventory);
                    break;

                case 3:
                    return;

                default:
                    System.out.println("Invalid admin option.");
            }
        }
    }

    private static void runUserModule(Scanner scanner, Inventory inventory) {
        while (true) {
            System.out.println("\n User Menu ");
            System.out.println("1. Filter by Builder");
            System.out.println("2. Filter by Type");
            System.out.println("3. Filter by Number of Strings");
            System.out.println("4. Filter by Wood");
            System.out.println("5. Show All Guitars");
            System.out.println("6. Exit User Module");
            System.out.print("Choose an option: ");
            int option = readInt(scanner);

            GuitarSpec filterSpec = new GuitarSpec(null, null, null, 0, null, null);

            switch (option) {
                case 1:
                    Builder[] builders = Builder.values();
                    showOptions("Select Builder", builders);
                    int bIndex = readInt(scanner) - 1;
                    if (isValidIndex(bIndex, builders))
                        filterSpec = new GuitarSpec(builders[bIndex], null, null, 0, null, null);
                    break;

                case 2:
                    Type[] types = Type.values();
                    showOptions("Select Type", types);
                    int tIndex = readInt(scanner) - 1;
                    if (isValidIndex(tIndex, types))
                        filterSpec = new GuitarSpec(null, null, types[tIndex], 0, null, null);
                    break;

                case 3:
                    System.out.print("Enter number of strings: ");
                    int strings = readInt(scanner);
                    filterSpec = new GuitarSpec(null, null, null, strings, null, null);
                    break;

                case 4:
                    Wood[] woods = Wood.values();
                    showOptions("Select Back Wood", woods);
                    int back = readInt(scanner) - 1;
                    showOptions("Select Top Wood", woods);
                    int top = readInt(scanner) - 1;
                    if (isValidIndex(back, woods) && isValidIndex(top, woods))
                        filterSpec = new GuitarSpec(null, null, null, 0, woods[back], woods[top]);
                    break;

                case 5:
                    showAllGuitars(inventory);
                    continue;

                case 6:
                    return;

                default:
                    System.out.println("Invalid option.");
                    continue;
            }

            List<Guitar> results = inventory.search(filterSpec);
            if (results.isEmpty()) {
                System.out.println("No matching guitars found.");
            } else {
                displayGuitars(results);
            }
        }
    }

    private static void showAllGuitars(Inventory inventory) {
        System.out.println("\n--- All Available Guitars ---");
        List<Guitar> allGuitars = inventory.search(new GuitarSpec(null, null, null, 0, null, null));
        displayGuitars(allGuitars);
    }

    private static void displayGuitars(List<Guitar> guitars) {
        for (Guitar guitar : guitars) {
            GuitarSpec spec = guitar.getSpec();
            System.out.println(spec.getBuilder() + " " + spec.getModel() + " | " +
                    spec.getType() + " | " + spec.getNumStrings() + " strings | " +
                    spec.getBackWood() + " back, " + spec.getTopWood() + " top | " +
                    "Price: $" + guitar.getPrice());
        }
    }

    private static <T> void showOptions(String title, T[] options) {
        System.out.println("\n" + title + ":");
        for (int i = 0; i < options.length; i++) {
            System.out.println((i + 1) + ". " + options[i]);
        }
        System.out.print("Your choice: ");
    }

    private static boolean isValidIndex(int index, Object[] array) {
        return index >= 0 && index < array.length;
    }

    private static int readInt(Scanner scanner) {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (Exception e) {
                System.out.print("Invalid input. Try again: ");
            }
        }
    }

    private static double readDouble(Scanner scanner) {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (Exception e) {
                System.out.print("Invalid price. Try again: ");
            }
        }
    }

    private static void initializeInventory(Inventory inventory) {
        inventory.addGuitar("V95693", 1499.95, Builder.FENDER, "Stratocastor", Type.ELECTRIC, 6, Wood.ALDER, Wood.ALDER);
        inventory.addGuitar("V9512", 1549.95, Builder.FENDER, "Stratocastor", Type.ELECTRIC, 6, Wood.ALDER, Wood.ALDER);
        inventory.addGuitar("X12345", 1999.95, Builder.MARTIN, "D-18", Type.ACOUSTIC, 12, Wood.MAHOGANY, Wood.ADIRONDACK);
    }
}
