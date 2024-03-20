package ui;

import java.util.Scanner;

public class ClientUI {
    private final ServerFacade serverFacade;
    private final Scanner scanner;

    public ClientUI(Integer port) {
        serverFacade = new ServerFacade(port);
        scanner = new Scanner(System.in);
    }

    public void start() {
        boolean quit = false;
        System.out.println("Welcome to 240 Chess! Type 'help' to get started.");
        while (!quit) {
            System.out.print(serverFacade.isLoggedIn() ? "[LOGGED_IN] " : "[LOGGED_OUT] ");
            System.out.print(">>> ");
            String command = scanner.nextLine().trim();
            String[] parts = command.split("\\s+");

            switch (parts[0]) {
                case "help":
                    serverFacade.displayHelp();
                    break;

                case "register":
                    if (parts.length == 4) {
                        String username = parts[1];
                        String password = parts[2];
                        String email = parts[3];
                        try {
                            serverFacade.register(username, password, email);
                            System.out.println("Registration successful!");
                        } catch (ResponseException e) {
                            System.out.println("Registration failed. Please try again.");
                        }
                    } else {
                        System.out.println("Invalid command. Usage: register <USERNAME> <PASSWORD> <EMAIL>");
                    }
                    break;

                case "login":
                    if (parts.length == 3) {
                        String username = parts[1];
                        String password = parts[2];
                        try {
                            serverFacade.login(username, password);
                            System.out.println("Login successful!");
                        } catch (ResponseException e) {
                            System.out.println("Login failed. Please try again.");
                        }
                    } else {
                        System.out.println("Invalid command. Usage: login <USERNAME> <PASSWORD>");
                    }
                    break;

                default:
                    System.out.println("Invalid command. Type 'help' for available commands.");
            }
        }
    }
}