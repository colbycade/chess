package ui;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;
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

                case "logout":
                    serverFacade.logout();
                    System.out.println("Logged out.");
                    break;

                case "create":
                    if (parts.length == 2) {
                        try {
                            String gameName = parts[1];
                            Integer gameID = serverFacade.createGame(serverFacade.getAuthToken(), gameName).gameID();
                            System.out.println("Game created with ID: " + gameID.toString());
                        } catch (ResponseException e) {
                            System.out.println("Failed to create game.");
                        }
                    } else {
                        System.out.println("Invalid command. Usage: create <NAME>");
                    }
                    break;

                case "list":
                    if (parts.length == 1) {
                        try {
                            Collection<GameData> games = serverFacade.listGames(serverFacade.getAuthToken()).games();
                            System.out.println("Available games:");
                            for (GameData game : games) {
                                System.out.println(game.toString());
                            }
                        } catch (ResponseException e) {
                            System.out.println("Failed to retrieve games.");
                        }
                    } else {
                        System.out.println("Invalid command. Usage: list");
                        break;
                    }

                case "join":
                    if (parts.length == 3) {
                        try {
                            Integer gameID = Integer.parseInt(parts[1]);
                            ChessGame.TeamColor clientColor = ChessGame.TeamColor.valueOf(parts[2].toUpperCase());
                            serverFacade.joinGame(serverFacade.getAuthToken(), clientColor, gameID);
                            System.out.println("Joined game " + gameID + " as " + clientColor);
                        } catch (ResponseException e) {
                            System.out.println("Failed to join game.");
                        }
                    } else {
                        System.out.println("Invalid command. Usage: join <gameID> [WHITE|BLACK]");
                    }
                    break;

                case "observe":
                    if (parts.length == 2) {
                        try {
                            Integer gameID = Integer.parseInt(parts[1]);
                            serverFacade.observeGame(serverFacade.getAuthToken(), gameID);
                            System.out.println("Observing game " + gameID);
                        } catch (ResponseException e) {
                            System.out.println("Failed to observe game.");
                        }
                    } else {
                        System.out.println("Invalid command. Usage: observe <gameID>");
                    }
                    break;
                case "quit":
                    quit = true;
                    System.out.println("Exiting the program.");
                    break;

                default:
                    System.out.println("Invalid command. Type 'help' for available commands.");
            }
        }
    }
}