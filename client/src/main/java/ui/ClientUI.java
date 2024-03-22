package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
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
        System.out.println(SET_TEXT_BOLD + SET_TEXT_COLOR_MAGENTA + SET_BG_COLOR_WHITE + "Welcome to 240 Chess! Type " +
                SET_TEXT_COLOR_BLUE + "help " + SET_TEXT_COLOR_MAGENTA + "to get started." + RESET_ALL);
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
                        if (games.isEmpty()) {
                            System.out.println(SET_TEXT_COLOR_RED + "No games available.");
                        } else {
                            System.out.println(SET_TEXT_COLOR_GREEN + "Available games:" + RESET_ALL);
                            int count = 0;
                            for (GameData game : games) {
                                count++;
                                // Display game information
                                System.out.println(SET_TEXT_COLOR_MAGENTA + "Game " + SET_TEXT_COLOR_YELLOW + count + SET_TEXT_COLOR_MAGENTA + ": " + RESET_ALL);
                                System.out.println(SET_TEXT_COLOR_BLACK + "  Game Name: " + SET_TEXT_COLOR_BLUE + game.gameName());
                                System.out.println(SET_TEXT_COLOR_BLACK + "  White Player: " + SET_TEXT_COLOR_BLUE +
                                        (game.whiteUsername() != null ? game.whiteUsername() : SET_TEXT_COLOR_LIGHT_GREY + "none"));
                                System.out.println(SET_TEXT_COLOR_BLACK + "  Black Player: " + SET_TEXT_COLOR_BLUE +
                                        (game.blackUsername() != null ? game.blackUsername() : SET_TEXT_COLOR_LIGHT_GREY + "none"));
                            }
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
                        Collection<GameData> games = serverFacade.listGames(serverFacade.getAuthToken()).games();
                        GameData game = games.stream()
                                .filter(g -> g.gameID().equals(gameID))
                                .findFirst()
                                .orElse(null);
                        if (game == null) {
                            throw new ResponseException("Game not found.");
                        }
                        displayGame(game);
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
                        Collection<GameData> games = serverFacade.listGames(serverFacade.getAuthToken()).games();
                        GameData game = games.stream()
                                .filter(g -> g.gameID().equals(gameID))
                                .findFirst()
                                .orElse(null);
                        if (game == null) {
                            throw new ResponseException("Game not found.");
                        }
                        displayGame(game);
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
                    System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Type " + SET_TEXT_COLOR_BLUE + "help " + SET_TEXT_COLOR_RED + "for available commands.");
            }
        }
    }

    public static void displayGame(GameData game) {
        // Display board from both perspectives
        ChessBoard board = game.game().getBoard();
        System.out.println(getBoardDisplay(board, ChessGame.TeamColor.WHITE));
        System.out.println(SET_BG_COLOR_BLACK + " ".repeat(30) + RESET_ALL);
        System.out.println(getBoardDisplay(board, ChessGame.TeamColor.BLACK));
        System.out.println();
    }

    public static String getBoardDisplay(ChessBoard board, ChessGame.TeamColor perspective) {
        StringBuilder boardDisplay = new StringBuilder();
        boardDisplay.append(ERASE_SCREEN);
        boardDisplay.append(SET_TEXT_BOLD + SET_TEXT_COLOR_BLUE + SET_BG_COLOR_WHITE);

        String columnLabels = "    a  b  c  d  e  f  g  h    " + RESET_ALL;
        boardDisplay.append(columnLabels).append("\n");

        int rowStart = perspective == ChessGame.TeamColor.WHITE ? 8 : 1;
        int rowEnd = perspective == ChessGame.TeamColor.WHITE ? 1 : 8;
        int rowDirection = perspective == ChessGame.TeamColor.WHITE ? -1 : 1;

        int colStart = perspective == ChessGame.TeamColor.WHITE ? 1 : 8;
        int colEnd = perspective == ChessGame.TeamColor.WHITE ? 8 : 1;
        int colDirection = perspective == ChessGame.TeamColor.WHITE ? 1 : -1;

        for (int row = rowStart; row != rowEnd + rowDirection; row += rowDirection) {
            boardDisplay.append(SET_TEXT_BOLD + SET_TEXT_COLOR_RED + SET_BG_COLOR_WHITE);
            boardDisplay.append(" ").append(row).append(" ").append(RESET_ALL).append(SET_TEXT_COLOR_BLACK);

            for (int col = colStart; col != colEnd + colDirection; col += colDirection) {
                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                String pieceString = getString(piece);
                boardDisplay.append(SET_TEXT_BOLD).append((row + col) % 2 == 0 ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY);
                boardDisplay.append(pieceString);
            }

            boardDisplay.append(SET_TEXT_BOLD + SET_TEXT_COLOR_RED + SET_BG_COLOR_WHITE);
            boardDisplay.append(" ").append(row).append(" ").append(RESET_ALL).append("\n");
        }

        boardDisplay.append(SET_TEXT_BOLD + SET_TEXT_COLOR_BLUE + SET_BG_COLOR_WHITE);
        boardDisplay.append(columnLabels);

        return boardDisplay.toString();
    }

    private static String getString(ChessPiece piece) {
        ChessGame.TeamColor teamColor = piece != null ? piece.getTeamColor() : null;
        ChessPiece.PieceType pieceType = piece != null ? piece.getPieceType() : null;
        String pieceString = SET_TEXT_BOLD + (teamColor == ChessGame.TeamColor.WHITE ? SET_TEXT_COLOR_WHITE : SET_TEXT_COLOR_BLACK);
        pieceString += switch (pieceType) {
            case KING -> BLACK_KING;
            case QUEEN -> BLACK_QUEEN;
            case BISHOP -> BLACK_BISHOP;
            case KNIGHT -> BLACK_KNIGHT;
            case ROOK -> BLACK_ROOK;
            case PAWN -> BLACK_PAWN;
            case null -> EMPTY;
        };
        return pieceString;
    }
}