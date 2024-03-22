package ui;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class ClientUI {
    private final ServerFacade serverFacade;
    private final Scanner scanner;

    public ClientUI(Integer port) {
        serverFacade = new ServerFacade(port);
        scanner = new Scanner(System.in);
    }

    public void start() {
        boolean quit = false;
        System.out.println(SET_TEXT_BOLD + SET_TEXT_COLOR_RED + SET_BG_COLOR_WHITE + "Welcome to 240 Chess! Type 'help' to get started.");
        while (!quit) {
            System.out.print(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_GREEN + SET_TEXT_FAINT);
            System.out.print(serverFacade.isLoggedIn() ? "[LOGGED_IN] " : "[LOGGED_OUT] ");
            System.out.print(">>> " + RESET_ALL + " ");
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
                            System.out.println(SET_TEXT_COLOR_GREEN + "Registration successful!");
                        } catch (ResponseException e) {
                            System.out.println(SET_TEXT_COLOR_RED + "Registration failed. Please try again.");
                        }
                    } else {
                        System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Usage: " + SET_TEXT_COLOR_BLUE + "register <USERNAME> <PASSWORD> <EMAIL>");
                    }
                    break;

                case "login":
                    if (parts.length == 3) {
                        String username = parts[1];
                        String password = parts[2];
                        try {
                            serverFacade.login(username, password);
                            System.out.println(SET_TEXT_COLOR_GREEN + "Login successful!");
                        } catch (ResponseException e) {
                            System.out.println(SET_TEXT_COLOR_RED + "Login failed. Please try again.");
                        }
                    } else {
                        System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Usage: " + SET_TEXT_COLOR_BLUE + "login <USERNAME> <PASSWORD>");
                    }
                    break;

                case "create":
                    if (parts.length == 2) {
                        try {
                            String gameName = parts[1];
                            Integer gameID = serverFacade.createGame(serverFacade.getAuthToken(), gameName).gameID();
                            System.out.println(SET_TEXT_COLOR_GREEN + "Game created with ID: " + SET_BG_COLOR_YELLOW + gameID.toString());
                        } catch (ResponseException e) {
                            System.out.println(SET_TEXT_COLOR_RED + "Failed to create game.");
                        }
                    } else {
                        System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Usage: " + SET_TEXT_COLOR_BLUE + "create <NAME>");
                    }
                    break;

                case "list":
                    if (parts.length == 1) {
                        try {
                            Collection<GameData> games = serverFacade.listGames(serverFacade.getAuthToken()).games();
                            System.out.println(SET_TEXT_COLOR_GREEN + "Available games:" + RESET_ALL);
                            for (GameData game : games) {
                                System.out.println(game.toString());
                            }
                        } catch (ResponseException e) {
                            System.out.println(SET_TEXT_COLOR_RED + "Failed to retrieve games.");
                        }
                    } else {
                        System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Usage: " + SET_TEXT_COLOR_BLUE + "list");
                        break;
                    }

                case "join":
                    if (parts.length == 3) {
                        try {
                            Integer gameID = Integer.parseInt(parts[1]);
                            ChessGame.TeamColor clientColor = ChessGame.TeamColor.valueOf(parts[2].toUpperCase());
                            serverFacade.joinGame(serverFacade.getAuthToken(), clientColor, gameID);
                            System.out.println(SET_TEXT_COLOR_GREEN + "Joined game " + SET_TEXT_COLOR_YELLOW + gameID + SET_TEXT_COLOR_GREEN + " as " + SET_TEXT_COLOR_YELLOW + clientColor);
                        } catch (ResponseException e) {
                            System.out.println(SET_TEXT_COLOR_RED + "Failed to join game.");
                        }
                    } else {
                        System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Usage: " + SET_TEXT_COLOR_BLUE + "join <gameID> [WHITE|BLACK]");
                    }
                    break;

                case "observe":
                    if (parts.length == 2) {
                        try {
                            Integer gameID = Integer.parseInt(parts[1]);
                            serverFacade.observeGame(serverFacade.getAuthToken(), gameID);
                            System.out.println(SET_TEXT_COLOR_GREEN + "Observing game " + SET_TEXT_COLOR_YELLOW + gameID);
                        } catch (ResponseException e) {
                            System.out.println(SET_TEXT_COLOR_RED + "Failed to observe game.");
                        }
                    } else {
                        System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Usage: " + SET_TEXT_COLOR_BLUE + "observe <gameID>");
                    }
                    break;

                case "logout":
                    if (parts.length == 1) {
                        try {
                            serverFacade.logout();
                            System.out.println(SET_TEXT_COLOR_GREEN + "Logged out.");
                        } catch (ResponseException e) {
                            System.out.println(SET_TEXT_COLOR_RED + "Failed to logout.");
                        }
                    } else {
                        System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Usage: " + SET_TEXT_COLOR_BLUE + "logout");
                    }
                    break;

                case "quit":
                    quit = true;
                    System.out.println(SET_TEXT_COLOR_RED + "Exiting the program.");
                    break;

                default:
                    System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Type " + SET_TEXT_COLOR_BLUE + "'help'" + SET_TEXT_COLOR_RED + "for available commands.");
            }
        }
    }
}