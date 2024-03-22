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
                    if (serverFacade.isLoggedIn()) {
                        System.out.println(SET_TEXT_COLOR_RED + "You are already logged in. Please logout before registering a new account.");
                        break;
                    }
                    if (parts.length != 4) {
                        System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Usage: " + SET_TEXT_COLOR_BLUE + "register <USERNAME> <PASSWORD> <EMAIL>");
                    }
                    String newUsername = parts[1];
                    String newPassword = parts[2];
                    String newEmail = parts[3];
                    try {
                        serverFacade.register(newUsername, newPassword, newEmail);
                        System.out.println(SET_TEXT_COLOR_GREEN + "Registration successful!");
                    } catch (ResponseException e) {
                        System.out.println(SET_TEXT_COLOR_RED + "Registration failed. Please try again.");
                    }
                    break;

                case "login":
                    if (serverFacade.isLoggedIn()) {
                        System.out.println(SET_TEXT_COLOR_RED + "You are already logged in.");
                        break;
                    }
                    if (parts.length != 3) {
                        System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Usage: " + SET_TEXT_COLOR_BLUE + "login <USERNAME> <PASSWORD>");
                        break;
                    }
                    String username = parts[1];
                    String password = parts[2];
                    try {
                        serverFacade.login(username, password);
                        System.out.println(SET_TEXT_COLOR_GREEN + "Login successful!");
                    } catch (ResponseException e) {
                        System.out.println(SET_TEXT_COLOR_RED + "Login failed. Please try again.");
                    }
                    break;

                case "create":
                    if (!serverFacade.isLoggedIn()) {
                        System.out.println(SET_TEXT_COLOR_RED + "You must be logged in to create a game.");
                        break;
                    }
                    if (parts.length != 2) {
                        System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Usage: " + SET_TEXT_COLOR_BLUE + "create <NAME>");
                    }
                    try {
                        String gameName = parts[1];
                        Integer gameID = serverFacade.createGame(serverFacade.getAuthToken(), gameName).gameID();
                        System.out.println(SET_TEXT_COLOR_GREEN + "Game created with ID: " + SET_TEXT_COLOR_YELLOW + gameID.toString());
                    } catch (ResponseException e) {
                        System.out.println(SET_TEXT_COLOR_RED + "Failed to create game.");
                    }
                    break;

                case "list":
                    if (!serverFacade.isLoggedIn()) {
                        System.out.println(SET_TEXT_COLOR_RED + "You must be logged in to list games.");
                        break;
                    }
                    if (parts.length != 1) {
                        System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Usage: " + SET_TEXT_COLOR_BLUE + "list");
                        break;
                    }
                    try {
                        Collection<GameData> games = serverFacade.listGames(serverFacade.getAuthToken()).games();
                        System.out.println(SET_TEXT_COLOR_GREEN + "Available games:" + RESET_ALL);
                        for (GameData game : games) {
                            System.out.println(game.toString());
                        }
                    } catch (ResponseException e) {
                        System.out.println(SET_TEXT_COLOR_RED + "Failed to retrieve games.");
                    }
                    break;

                case "join":
                    if (!serverFacade.isLoggedIn()) {
                        System.out.println(SET_TEXT_COLOR_RED + "You must be logged in to join a game.");
                        break;
                    }
                    if (parts.length != 3) {
                        System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Usage: " + SET_TEXT_COLOR_BLUE + "join <gameID> [WHITE|BLACK]");
                        break;
                    }
                    try {
                        Integer gameID = Integer.parseInt(parts[1]);
                        ChessGame.TeamColor clientColor = ChessGame.TeamColor.valueOf(parts[2].toUpperCase());
                        serverFacade.joinGame(serverFacade.getAuthToken(), clientColor, gameID);
                        System.out.println(SET_TEXT_COLOR_GREEN + "Joined game " + SET_TEXT_COLOR_YELLOW + gameID + SET_TEXT_COLOR_GREEN + " as " + SET_TEXT_COLOR_YELLOW + clientColor);
                    } catch (ResponseException e) {
                        System.out.println(SET_TEXT_COLOR_RED + "Failed to join game.");
                    }
                    break;

                case "observe":
                    if (!serverFacade.isLoggedIn()) {
                        System.out.println(SET_TEXT_COLOR_RED + "You must be logged in to observe a game.");
                        break;
                    }
                    if (parts.length != 2) {
                        System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Usage: " + SET_TEXT_COLOR_BLUE + "observe <gameID>");
                        break;
                    }
                    try {
                        Integer gameID = Integer.parseInt(parts[1]);
                        serverFacade.observeGame(serverFacade.getAuthToken(), gameID);
                        System.out.println(SET_TEXT_COLOR_GREEN + "Observing game " + SET_TEXT_COLOR_YELLOW + gameID);
                    } catch (ResponseException e) {
                        System.out.println(SET_TEXT_COLOR_RED + "Failed to observe game.");
                    }
                    break;

                case "logout":
                    if (!serverFacade.isLoggedIn()) {
                        System.out.println(SET_TEXT_COLOR_RED + "You are already logged out.");
                        break;
                    }
                    if (parts.length != 1) {
                        System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Usage: " + SET_TEXT_COLOR_BLUE + "logout");
                        break;
                    }
                    try {
                        serverFacade.logout();
                        System.out.println(SET_TEXT_COLOR_GREEN + "Logged out.");
                    } catch (ResponseException e) {
                        System.out.println(SET_TEXT_COLOR_RED + "Failed to logout.");
                    }
                    break;

                case "quit":
                    quit = true;
                    System.out.println(SET_TEXT_COLOR_RED + "Exiting the program.");
                    break;

                default:
                    System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Type " + SET_TEXT_COLOR_BLUE + "help" + SET_TEXT_COLOR_RED + "for available commands.");
            }
        }
    }
}