package chess;

import java.util.Map;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private ChessPiece[][] board = new ChessPiece[8][8];

    public ChessBoard() {
    }

    /**
     * @return String representation of ChessBoard object
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  1 2 3 4 5 6 7 8\n"); // Column labels
        for (int row = 7; row >= 0; row--) { // Start from the top (row 8) and go down to row 1
            sb.append(row + 1).append(" "); // Row number
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = board[row][col];
                if (piece == null) {
                    sb.append("- "); // No piece at this position
                } else {
                    sb.append(piece.getTeamColor() == ChessGame.TeamColor.WHITE
                                    ? whitePieceToChar.get(piece.getPieceType())
                                    : blackPieceToChar.get(piece.getPieceType()))
                            .append(" "); // Get the symbol of the piece
                }
            }
            sb.append(row + 1).append("\n"); // Row number
        }
        sb.append("  1 2 3 4 5 6 7 8\n"); // Column labels


        return sb.toString();
    }

    final static Map<ChessPiece.PieceType, String> whitePieceToChar = Map.of(
            ChessPiece.PieceType.PAWN, "♙",    // White Pawn
            ChessPiece.PieceType.KNIGHT, "♘",  // White Knight
            ChessPiece.PieceType.ROOK, "♖",    // White Rook
            ChessPiece.PieceType.QUEEN, "♕",   // White Queen
            ChessPiece.PieceType.KING, "♔",    // White King
            ChessPiece.PieceType.BISHOP, "♗"); // White Bishop

    final static Map<ChessPiece.PieceType, String> blackPieceToChar = Map.of(
            ChessPiece.PieceType.PAWN, "♟",    // Black Pawn
            ChessPiece.PieceType.KNIGHT, "♞",  // Black Knight
            ChessPiece.PieceType.ROOK, "♜",    // Black Rook
            ChessPiece.PieceType.QUEEN, "♛",   // Black Queen
            ChessPiece.PieceType.KING, "♚",    // Black King
            ChessPiece.PieceType.BISHOP, "♝"); // Black Bishop


    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[8 - position.getRow()][position.getColumn()] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow()][position.getColumn()];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        // Clear the board
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = null;
            }
        }
        // Set up white pieces
        for (int col = 0; col < 8; col++) {
            board[6][col] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN); // White pawns
        }
        board[7][0] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        board[7][7] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        board[7][1] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        board[7][6] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        board[7][2] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        board[7][5] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        board[7][3] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN);
        board[7][4] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING);

        // Set up black pieces
        for (int col = 0; col < 8; col++) {
            board[1][col] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN); // Black pawns
        }
        board[0][0] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
        board[0][7] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
        board[0][1] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        board[0][6] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        board[0][2] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
        board[0][5] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
        board[0][3] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN);
        board[0][4] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING);

    }
}
