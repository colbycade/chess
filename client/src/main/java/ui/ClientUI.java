package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class ClientUI {
    private final ServerFacade serverFacade;
    private final Scanner scanner;
    
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
            System.out.print(serverFacade.isLoggedIn() ? "[LOGGED_IN] " : "[LOGGED_OUT] ");
            System.out.print(">>> " + RESET_ALL + " ");
            String command = scanner.nextLine().trim();
            String[] parts = command.split("\\s+");
            
            // Shared commands
            switch (parts[0]) {
                case "help" -> serverFacade.displayHelp();
                case "quit" -> {
                    quit = true;
                    System.out.println(SET_TEXT_COLOR_RED + "Exiting the program.");
                }
            }
            
            // State-specific commands
            currentState = switch (currentState) {
                case LOGGED_OUT -> handleLoggedOut(parts);
                case LOGGED_IN -> handleLoggedIn(parts);
                case GAMEPLAY -> handleGameplay(parts);
            };
        }
    }
    
    private ClientState handleLoggedOut(String[] parts) {
        switch (parts[0]) {
            // Handle pre-login commands
            
            case "register" -> {    // Handle registration
                
                return ClientState.LOGGED_IN;
            }
            case "login" -> {       // Handle login
                
                return ClientState.LOGGED_IN;
            }
            
            // Handle pre-game commands being used in the wrong state
            case "create", "list", "join", "observe", "logout" -> {
            
            }
            
            // Handle gameplay commands being used in the wrong state
            case "redraw", "highlight", "make_move", "resign", "leave" -> {
            
            }
            
            default -> System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Type " +
                    SET_TEXT_COLOR_BLUE + "help " + SET_TEXT_COLOR_RED + "for available commands.");
        }
        return ClientState.LOGGED_OUT;
    }
    
    private ClientState handleLoggedIn(String[] parts) {
        switch (parts[0]) {
            // Handle pre-game commands
            
            case "create" -> {      // Create a new game
            
            }
            case "list" -> {        // List all active games
            
            }
            case "join" -> {        // Join a game as a player
            
            }
            case "observe" -> {     // Join a game as an observer
            
            }
            case "logout" -> {      // Go back to logged out state
                
                return ClientState.LOGGED_OUT;
            }
            
            // Handle pre-login commands being used in the wrong state
            case "register", "login" -> {
            
            }
            
            // Handle gameplay commands being used in the wrong state
            case "redraw", "highlight", "make_move", "resign", "leave" -> {
            
            }
            
            default -> System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Type " +
                    SET_TEXT_COLOR_BLUE + "help " + SET_TEXT_COLOR_RED + "for available commands.");
        }
        return ClientState.LOGGED_IN;
    }
    
    private ClientState handleGameplay(String[] parts) {
        switch (parts[0]) {
            // Handle gameplay commands
            
            case "redraw" -> {      // Redraw the board
            
            }
            
            case "highlight" -> {   // Highlight available moves
            
            }
            
            case "make_move" -> {   // Make a move
            
            }
            
            case "resign" -> {      // Resign the game (but don't leave)
            
            }
            
            case "leave" -> {       // Leave the game (go back to pregame state)
                
                return ClientState.LOGGED_IN;
            }
            
            // Handle pre-login commands being used in the wrong state
            case "register", "login" -> {
            
            }
            
            // Handle pre-game commands being used in the wrong state
            case "create", "list", "join", "observe", "logout" -> {
            
            }
            
            default -> System.out.println(SET_TEXT_COLOR_RED + "Invalid command. Type " +
                    SET_TEXT_COLOR_BLUE + "help " + SET_TEXT_COLOR_RED + "for available commands.");
        }
        return ClientState.GAMEPLAY;
    }
    
    private static void displayGame(GameData game) {
        // Display board from both perspectives
        ChessBoard board = game.game().getBoard();
        System.out.println(getBoardDisplay(board, ChessGame.TeamColor.WHITE));
        System.out.println(SET_BG_COLOR_BLACK + " ".repeat(30) + RESET_ALL);
        System.out.println(getBoardDisplay(board, ChessGame.TeamColor.BLACK));
        System.out.println();
    }
    
    // Create a string representation of the board
    private static String getBoardDisplay(ChessBoard board, ChessGame.TeamColor perspective) {
        StringBuilder boardDisplay = new StringBuilder();
        boardDisplay.append(ERASE_SCREEN);
        boardDisplay.append(SET_TEXT_BOLD + SET_TEXT_COLOR_BLUE + SET_BG_COLOR_WHITE);
        
        String whiteColumnLabels = "    a  b  c  d  e  f  g  h    " + RESET_ALL;
        String blackColumnLabels = "    h  g  f  e  d  c  b  a    " + RESET_ALL;
        String columnLabels = perspective == ChessGame.TeamColor.WHITE ? whiteColumnLabels : blackColumnLabels;
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