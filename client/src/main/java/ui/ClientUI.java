package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPosition;
import model.GameData;

import java.util.Collection;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class ClientUI {
    private final ServerFacade serverFacade;
    private final Scanner scanner;
    private GameData currGameData;
    private ChessGame.TeamColor currColor;
    
    public ClientUI(Integer port) {
        serverFacade = new ServerFacade(port);
        scanner = new Scanner(System.in);
    }
    
    private enum ClientState {
        LOGGED_OUT,
        LOGGED_IN,
        GAMEPLAY
    }
    
    public void start() {
        boolean quit = false;
        ClientState currentState = ClientState.LOGGED_OUT;
        
        System.out.println(SET_TEXT_BOLD + SET_TEXT_COLOR_MAGENTA + SET_BG_COLOR_WHITE + "Welcome to 240 Chess! Type " +
                SET_TEXT_COLOR_BLUE + "help " + SET_TEXT_COLOR_MAGENTA + "to get started." + RESET_ALL);
        while (!quit) {
            System.out.print(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_GREEN + SET_TEXT_FAINT);
            System.out.print(currentState == ClientState.LOGGED_IN ? "[LOGGED_IN] " :
                    currentState == ClientState.LOGGED_OUT ? "[LOGGED_OUT] " : "[IN_GAME] " + " ");
            System.out.print(">>> " + RESET_ALL + " ");
            String command = scanner.nextLine().trim();
            String[] parts = command.split("\\s+");
            
            switch (parts[0]) {
                // Shared commands
                case "help" -> serverFacade.displayHelp();
                case "quit" -> {
                    quit = true;
                    System.out.println(SET_TEXT_COLOR_RED + "Exiting the program.");
                }
                // State-specific commands
                default -> currentState = switch (currentState) {
                    case LOGGED_OUT -> handleLoggedOut(parts);
                    case LOGGED_IN -> handleLoggedIn(parts);
                    case GAMEPLAY -> handleGameplay(parts);
                };
            }
        }
    }
    
    private ClientState handleLoggedOut(String[] parts) {
        String command = parts[0];
        switch (command) {
            // Handle pre-login commands
            case "register" -> {
                // Handle registration
                try {
                    String newUsername = parts[1];
                    String newPassword = parts[2];
                    String newEmail = parts[3];
                    serverFacade.register(newUsername, newPassword, newEmail);
                    System.out.println(SET_TEXT_COLOR_GREEN + "Registration successful!");
                    return ClientState.LOGGED_IN;
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Usage: " + SET_TEXT_COLOR_BLUE + "register <USERNAME> <PASSWORD> <EMAIL>");
                } catch (ResponseException e) {
                    System.out.println(SET_TEXT_COLOR_RED + "Registration failed. Please try again.");
                }
            }
            case "login" -> {
                // Handle login
                try {
                    String username = parts[1];
                    String password = parts[2];
                    serverFacade.login(username, password);
                    System.out.println(SET_TEXT_COLOR_GREEN + "Login successful!");
                    return ClientState.LOGGED_IN;
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Usage: " + SET_TEXT_COLOR_BLUE + "login <USERNAME> <PASSWORD>");
                } catch (ResponseException e) {
                    System.out.println(SET_TEXT_COLOR_RED + "Login failed. Please try again.");
                }
            }
            
            // Handle pre-game commands being used in the wrong state
            case "create", "list", "join", "observe", "logout" -> System.out.println(SET_TEXT_COLOR_RED
                    + "Please log in to use the command: " + SET_TEXT_COLOR_BLUE + command);
            
            // Handle gameplay commands being used in the wrong state
            case "redraw", "highlight", "make_move", "resign", "leave" -> System.out.println(SET_TEXT_COLOR_RED
                    + "Please log in and join a game to use the command: " + SET_TEXT_COLOR_BLUE + command);
            
            default -> System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Type " +
                    SET_TEXT_COLOR_BLUE + "help " + SET_TEXT_COLOR_RED + "for available commands.");
        }
        return ClientState.LOGGED_OUT;
    }
    
    private ClientState handleLoggedIn(String[] parts) {
        String command = parts[0];
        switch (command) {
            // Handle pre-game commands
            case "create" -> {
                // Create a new game
                try {
                    String gameName = parts[1];
                    Integer gameID = serverFacade.createGame(serverFacade.getAuthToken(), gameName).gameID();
                    System.out.println(SET_TEXT_COLOR_GREEN + "Game created with ID: " + SET_TEXT_COLOR_YELLOW + gameID.toString());
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Usage: " + SET_TEXT_COLOR_BLUE + "create <NAME>");
                } catch (ResponseException e) {
                    System.out.println(SET_TEXT_COLOR_RED + "Failed to create game.");
                }
            }
            case "list" -> {
                // List all active games
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
                
            }
            case "join" -> {
                // Join a game as a player
                try {
                    Integer gameID = Integer.parseInt(parts[1]);
                    ChessGame.TeamColor clientColor = ChessGame.TeamColor.valueOf(parts[2].toUpperCase());
                    serverFacade.joinGame(serverFacade.getAuthToken(), clientColor, gameID);
                    System.out.println(SET_TEXT_COLOR_GREEN + "Joined game " + SET_TEXT_COLOR_YELLOW + gameID + SET_TEXT_COLOR_GREEN + " as " + SET_TEXT_COLOR_YELLOW + clientColor);
                    Collection<GameData> games = serverFacade.listGames(serverFacade.getAuthToken()).games();
                    GameData gameData = games.stream()
                            .filter(g -> g.gameID().equals(gameID))
                            .findFirst()
                            .orElse(null);
                    if (gameData == null) {
                        throw new ResponseException("Game not found.");
                    }
                    ChessBoard board = gameData.game().getBoard();
                    UIUtility.displayBoard(board, clientColor);
                    currColor = clientColor;
                    currGameData = gameData;
                    return ClientState.GAMEPLAY;
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Usage: " + SET_TEXT_COLOR_BLUE + "join <gameID> [WHITE|BLACK]");
                } catch (ResponseException | NumberFormatException e) {
                    System.out.println(SET_TEXT_COLOR_RED + "Failed to join game.");
                }
            }
            case "observe" -> {
                // Join a game as an observer
                try {
                    Integer gameID = Integer.parseInt(parts[1]);
                    serverFacade.observeGame(serverFacade.getAuthToken(), gameID);
                    System.out.println(SET_TEXT_COLOR_GREEN + "Observing game " + SET_TEXT_COLOR_YELLOW + gameID);
                    Collection<GameData> games = serverFacade.listGames(serverFacade.getAuthToken()).games();
                    GameData gameData = games.stream()
                            .filter(g -> g.gameID().equals(gameID))
                            .findFirst()
                            .orElse(null);
                    if (gameData == null) {
                        throw new ResponseException("Game not found.");
                    }
                    ChessBoard board = gameData.game().getBoard();
                    UIUtility.displayBoard(board, ChessGame.TeamColor.WHITE);
                    currGameData = gameData;
                    return ClientState.GAMEPLAY;
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Usage: " + SET_TEXT_COLOR_BLUE + "observe <gameID>");
                } catch (ResponseException | NumberFormatException e) {
                    System.out.println(SET_TEXT_COLOR_RED + "Failed to observe game.");
                }
            }
            case "logout" -> {
                // Go back to logged out state
                try {
                    serverFacade.logout();
                    System.out.println(SET_TEXT_COLOR_GREEN + "Logged out.");
                    return ClientState.LOGGED_OUT;
                } catch (ResponseException e) {
                    System.out.println(SET_TEXT_COLOR_RED + "Failed to logout.");
                }
            }
            
            // Handle pre-login commands being used in the wrong state
            case "register", "login" -> System.out.println(SET_TEXT_COLOR_RED + "You must first log out to "
                    + SET_TEXT_COLOR_BLUE + command);
            
            // Handle gameplay commands being used in the wrong state
            case "redraw", "highlight", "make_move", "resign", "leave" -> System.out.println(SET_TEXT_COLOR_RED
                    + "Please join a game to use the command: " + SET_TEXT_COLOR_BLUE + command);
            
            default -> System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Type " +
                    SET_TEXT_COLOR_BLUE + "help " + SET_TEXT_COLOR_RED + "for available commands.");
        }
        return ClientState.LOGGED_IN;
    }
    
    private ClientState handleGameplay(String[] parts) {
        String command = parts[0];
        switch (command) {
            // Handle gameplay commands
            case "redraw" ->        // Redraw the board
                    UIUtility.displayBoard(currGameData.game().getBoard(), currColor);
            case "highlight" -> {   // Highlight available moves
                try {
                    ChessPosition piecePosition = UIUtility.parsePosition(parts[1]);
                    UIUtility.highlightMoves(currGameData.game(), piecePosition);
                    
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Usage: " + SET_TEXT_COLOR_BLUE +
                            "highlight <POSITION> " + SET_TEXT_COLOR_RED + "(e.g. e2)");
                } catch (IllegalArgumentException e) {
                    System.out.println(SET_TEXT_COLOR_RED + "Invalid command: " + e.getMessage());
                }
            }
            case "make_move" -> {   // Make a move
                try {
                    throw new ResponseException("Not implemented.");
                } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
                    System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Usage: " + SET_TEXT_COLOR_BLUE +
                            "make_move <CURRENT POSITION> <TARGET POSITION>");
                } catch (ResponseException e) {
                    System.out.println(SET_TEXT_COLOR_RED + "Failed to make move.");
                }
            }
            case "resign" -> {      // Resign the game (but don't leave)
            
            }
            case "leave" -> {       // Leave the game (go back to pregame state)
                
                currColor = null;
                currGameData = null;
                return ClientState.LOGGED_IN;
            }
            
            // Handle pre-login commands being used in the wrong state
            case "register", "login" -> System.out.println(SET_TEXT_COLOR_RED
                    + "You must leave the current game and log out to use the command: "
                    + SET_TEXT_COLOR_BLUE + command);
            
            // Handle pre-game commands being used in the wrong state
            case "create", "list", "join", "observe", "logout" -> System.out.println(SET_TEXT_COLOR_RED
                    + "You must leave the current game to use the command: " + SET_TEXT_COLOR_BLUE + command);
            
            default -> System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Type " +
                    SET_TEXT_COLOR_BLUE + "help " + SET_TEXT_COLOR_RED + "for available commands.");
        }
        return ClientState.GAMEPLAY;
    }
}