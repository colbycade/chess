package ui;

import chess.*;
import model.GameData;
import websocket.messages.LoadGame;
import websocket.messages.Error;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.util.Collection;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class ChessClient implements ServerMessageObserver {
    private final ServerFacade serverFacade;
    private final Scanner scanner;
    private boolean running = false;
    
    public ChessClient(String host, int port) {
        serverFacade = new ServerFacade(host, port, this);
        scanner = new Scanner(System.in);
    }
    
    @Override
    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> loadGame(((LoadGame) message).gameData());
            case ERROR -> displayErrorMessage(((Error) message).errorMessage());
            case NOTIFICATION -> displayMessage(((Notification) message).message());
            default -> throw new IllegalStateException("Unexpected value: " + message.getServerMessageType());
        }
        // Reprint prompt
        System.out.print(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_GREEN + SET_TEXT_FAINT);
        System.out.print(" [IN_GAME]    ");
        System.out.print(">>> " + RESET_ALL + " ");
    }
    
    public void loadGame(GameData gameData) {
        ChessBoard board = gameData.game().getBoard();
        UIUtility.displayBoard(board, serverFacade.getClientColor());
        serverFacade.setCurrGameData(gameData);
    }
    
    public void displayMessage(String message) {
        System.out.println(SET_TEXT_COLOR_GREEN + message);
    }
    
    public void displayErrorMessage(String message) {
        System.out.println(SET_TEXT_COLOR_RED + "Error: " + message);
    }
    
    private enum ClientState {
        LOGGED_OUT,
        LOGGED_IN,
        GAMEPLAY
    }
    
    public void stop() {
        running = false;
    }
    
    public void start() {
        running = true;
        ClientState currentState = ClientState.LOGGED_OUT;
        
        System.out.println(SET_TEXT_BOLD + SET_TEXT_COLOR_MAGENTA + SET_BG_COLOR_WHITE + "Welcome to 240 Chess! Type " +
                SET_TEXT_COLOR_BLUE + "help " + SET_TEXT_COLOR_MAGENTA + "to get started." + RESET_ALL);
        while (running) {
            System.out.print(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_GREEN + SET_TEXT_FAINT);
            System.out.print(currentState == ClientState.LOGGED_IN ? " [LOGGED_IN]  " :
                    currentState == ClientState.LOGGED_OUT ? " [LOGGED_OUT] " : " [IN_GAME]    ");
            System.out.print(">>> " + RESET_ALL + " ");
            String command = scanner.nextLine().trim();
            String[] parts = command.split("\\s+");
            
            switch (parts[0]) {
                // Shared commands
                case "help" -> displayHelp(currentState);
                case "quit" -> {
                    System.out.println(SET_TEXT_COLOR_RED + "Exiting the program.");
                    if (serverFacade.getCurrGameData() != null) {
                        try {
                            serverFacade.leaveGame(serverFacade.getCurrGameData().gameID());
                        } catch (ResponseException e) {
                            System.out.println(SET_TEXT_COLOR_RED + "Failed to leave game upon quiting.");
                        }
                    }
                    stop();
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
                    System.out.println(SET_TEXT_COLOR_GREEN + "Joined game " + SET_TEXT_COLOR_YELLOW + gameID +
                            SET_TEXT_COLOR_GREEN + " as " + SET_TEXT_COLOR_YELLOW + clientColor);
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
                    UIUtility.displayBoard(serverFacade.getCurrGameData().game().getBoard(), serverFacade.getClientColor());
            case "highlight" -> {   // Highlight available moves
                try {
                    ChessPosition piecePosition = UIUtility.parsePosition(parts[1]);
                    UIUtility.highlightMoves(serverFacade.getCurrGameData().game(), piecePosition);
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Usage: " + SET_TEXT_COLOR_BLUE +
                            "highlight <POSITION> " + SET_TEXT_COLOR_RED + "(e.g. e2)");
                } catch (IllegalArgumentException e) {
                    System.out.println(SET_TEXT_COLOR_RED + "Invalid command: " + e.getMessage());
                }
            }
            case "make_move" -> {   // Make a move
                try {
                    // Validate and parse move
                    ChessPosition currPos = UIUtility.parsePosition(parts[1]);
                    ChessPosition targetPos = UIUtility.parsePosition(parts[2]);
                    ChessPiece piece = serverFacade.getCurrGameData().game().getBoard().getPiece(currPos);
                    ChessPiece.PieceType pieceType = piece != null ? piece.getPieceType() : null;
                    ChessMove move;
                    if (pieceType == ChessPiece.PieceType.PAWN && targetPos.getRow() == 1 || targetPos.getRow() == 8) {
                        // Pawn promotion
                        ChessPiece.PieceType promotionType = ChessPiece.PieceType.valueOf(parts[3]);
                        move = new ChessMove(currPos, targetPos, promotionType);
                    } else {
                        move = new ChessMove(currPos, targetPos, null);
                    }
                    // Make move
                    serverFacade.makeMove(serverFacade.getCurrGameData().gameID(), move);
                } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
                    System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Usage: " + SET_TEXT_COLOR_BLUE +
                            "make_move <CURRENT POSITION> <TARGET POSITION> [<PROMOTION TYPE>]");
                } catch (ResponseException e) {
                    System.out.println(SET_TEXT_COLOR_RED + "Failed to make move.");
                }
            }
            case "resign" -> {      // Resign the game (but don't leave)
                try {
                    serverFacade.resignGame(serverFacade.getCurrGameData().gameID());
                } catch (ResponseException e) {
                    System.out.println(SET_TEXT_COLOR_RED + "Failed to resign from game.");
                }
            }
            case "leave" -> {       // Leave the game (go back to pregame state)
                try {
                    serverFacade.leaveGame(serverFacade.getCurrGameData().gameID());
                    return ClientState.LOGGED_IN;
                } catch (ResponseException e) {
                    System.out.println(SET_TEXT_COLOR_RED + "Failed to leave game.");
                }
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
    
    private static void displayHelp(ClientState currentState) {
        switch (currentState) {
            case LOGGED_OUT -> System.out.println(preLoginHelp);
            case LOGGED_IN -> System.out.println(postLoginHelp);
            case GAMEPLAY -> System.out.println(inGameHelp);
        }
    }
    
    static final String preLoginHelp =
            SET_TEXT_COLOR_BLUE + "   register <USERNAME> <PASSWORD> <EMAIL> " + SET_TEXT_COLOR_MAGENTA + "- to create an account\n" +
                    SET_TEXT_COLOR_BLUE + "   login <USERNAME> <PASSWORD> " + SET_TEXT_COLOR_MAGENTA + "- to play chess\n" +
                    SET_TEXT_COLOR_BLUE + "   quit " + SET_TEXT_COLOR_MAGENTA + "- to quit\n" +
                    SET_TEXT_COLOR_BLUE + "   help " + SET_TEXT_COLOR_MAGENTA + "- list possible commands\n" + RESET_ALL;
    static final String postLoginHelp =
            SET_TEXT_COLOR_BLUE + "   create <NAME> " + SET_TEXT_COLOR_MAGENTA + "- a game\n" +
                    SET_TEXT_COLOR_BLUE + "   list " + SET_TEXT_COLOR_MAGENTA + "- all games\n" +
                    SET_TEXT_COLOR_BLUE + "   join <gameID> [WHITE|BLACK] " + SET_TEXT_COLOR_MAGENTA + "- to join a game\n" +
                    SET_TEXT_COLOR_BLUE + "   observe <gameID> " + SET_TEXT_COLOR_MAGENTA + "- a game\n" +
                    SET_TEXT_COLOR_BLUE + "   logout " + SET_TEXT_COLOR_MAGENTA + "- when you are done playing chess\n" +
                    SET_TEXT_COLOR_BLUE + "   quit " + SET_TEXT_COLOR_MAGENTA + "- to quit\n" +
                    SET_TEXT_COLOR_BLUE + "   help " + SET_TEXT_COLOR_MAGENTA + "- list possible commands\n" + RESET_ALL;
    static final String inGameHelp =
            SET_TEXT_COLOR_BLUE + "   redraw " + SET_TEXT_COLOR_MAGENTA + "- to redraw the board\n" +
                    SET_TEXT_COLOR_BLUE + "   highlight <POSITION> " + SET_TEXT_COLOR_MAGENTA + "- to show available moves\n" +
                    SET_TEXT_COLOR_BLUE + "   make_move <CURRENT POSITION> <TARGET POSITION> " + SET_TEXT_COLOR_MAGENTA + "- to move a piece\n" +
                    "      * If move involves a pawn promotion, add 'Q', 'R', 'N', or 'B' after target position, e.g., 'f7 f8 Q'\n" +
                    SET_TEXT_COLOR_BLUE + "   resign " + SET_TEXT_COLOR_MAGENTA + "- to resign from the game (without leaving)\n" +
                    SET_TEXT_COLOR_BLUE + "   leave " + SET_TEXT_COLOR_MAGENTA + "- to leave the game\n" +
                    SET_TEXT_COLOR_BLUE + "   quit " + SET_TEXT_COLOR_MAGENTA + "- to quit\n" +
                    SET_TEXT_COLOR_BLUE + "   help " + SET_TEXT_COLOR_MAGENTA + "- list possible commands\n" + RESET_ALL;
}