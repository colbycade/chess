package ui;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

import static ui.EscapeSequences.*;

public class UIUtility {
    
    static void displayBoard(ChessBoard board, ChessGame.TeamColor perspective) {
        // display the board with no highlighted squares
        printBoard(board, perspective, null);
    }
    
    static void highlightMoves(ChessGame game, ChessPosition piecePosition) {
        // validate the piece position
        ChessBoard board = game.getBoard();
        ChessGame.TeamColor currColor = game.getTeamTurn();
        if (board.getPiece(piecePosition) == null) {
            throw new IllegalArgumentException("No piece at position " + piecePosition);
        }
        ChessGame.TeamColor pieceColor = board.getPiece(piecePosition).getTeamColor();
        if (pieceColor != currColor) {
            throw new IllegalArgumentException("It is not " + pieceColor + "'s turn to move.");
        }
        if (game.getWinner() != null) {
            throw new IllegalArgumentException("No moves available. The game is over.");
        }
        // get valid squares to highlight
        Collection<ChessMove> moves = game.validMoves(piecePosition);
        ArrayList<ChessPosition> endPositions = new ArrayList<>();
        for (ChessMove move : moves) {
            endPositions.add(move.getEndPosition());
        }
        // display the board with highlighted squares
        printBoard(board, currColor, endPositions);
    }
    
    private static void printBoard(ChessBoard board, ChessGame.TeamColor perspective,
                                   Collection<ChessPosition> endPositions) {
        StringBuilder boardDisplay = new StringBuilder();
        boardDisplay.append(ERASE_SCREEN);
        boardDisplay.append(SET_TEXT_BOLD + SET_TEXT_COLOR_BLUE + SET_BG_COLOR_WHITE);
        
        String whiteColumnLabels = "    a  b  c  d  e  f  g  h    " + RESET_ALL;
        String blackColumnLabels = "    h  g  f  e  d  c  b  a    " + RESET_ALL;
        String columnLabels = perspective == ChessGame.TeamColor.WHITE ? whiteColumnLabels : blackColumnLabels;
        boardDisplay.append(columnLabels).append("\n");
        
        int rowStart = perspective == ChessGame.TeamColor.BLACK ? 1 : 8;
        int rowEnd = perspective == ChessGame.TeamColor.BLACK ? 8 : 1;
        int rowDirection = perspective == ChessGame.TeamColor.BLACK ? 1 : -1;
        
        int colStart = perspective == ChessGame.TeamColor.BLACK ? 8 : 1;
        int colEnd = perspective == ChessGame.TeamColor.BLACK ? 1 : 8;
        int colDirection = perspective == ChessGame.TeamColor.BLACK ? -1 : 1;
        
        for (int row = rowStart; row != rowEnd + rowDirection; row += rowDirection) {
            boardDisplay.append(SET_TEXT_BOLD + SET_TEXT_COLOR_RED + SET_BG_COLOR_WHITE);
            boardDisplay.append(" ").append(row).append(" ").append(RESET_ALL).append(SET_TEXT_COLOR_BLACK);
            
            for (int col = colStart; col != colEnd + colDirection; col += colDirection) {
                ChessPosition currentPosition = new ChessPosition(row, col);
                
                // Check if the square should be highlighted
                boolean isHighlighted = false;
                if (endPositions != null) {
                    isHighlighted = endPositions.contains(currentPosition);
                }
                
                // Apply a different background color for highlighted squares
                String bgColor = isHighlighted ? SET_BG_COLOR_YELLOW : ((row + col) % 2 == 0 ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY);
                ChessPiece piece = board.getPiece(currentPosition);
                String pieceString = getString(piece);
                
                boardDisplay.append(SET_TEXT_BOLD).append(bgColor);
                boardDisplay.append(pieceString);
            }
            
            boardDisplay.append(SET_TEXT_BOLD + SET_TEXT_COLOR_RED + SET_BG_COLOR_WHITE);
            boardDisplay.append(" ").append(row).append(" ").append(RESET_ALL).append("\n");
        }
        
        boardDisplay.append(SET_TEXT_BOLD + SET_TEXT_COLOR_BLUE + SET_BG_COLOR_WHITE);
        boardDisplay.append(columnLabels);
        
        System.out.println();
        System.out.println(boardDisplay);
    }
    
    
    static ChessPosition parsePosition(String position) {
        if (position == null || position.length() != 2) {
            throw new IllegalArgumentException("Invalid chess position: " + position);
        }
        
        // Convert char to int (1-indexed)
        int row = position.charAt(1) - '0';
        int col = position.charAt(0) - 'a' + 1;
        
        if (row < 1 || row > 8 || col < 1 || col > 8) {
            throw new IllegalArgumentException("Invalid chess position (Out of Bounds): " + position);
        }
        
        return new ChessPosition(row, col);
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
