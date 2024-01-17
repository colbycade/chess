package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * @return Unicode String representation of ChessPiece object
     */
    @Override
    public String toString() {
        return this.getTeamColor() == ChessGame.TeamColor.WHITE
                ? whitePieceToChar.get(this.getPieceType())
                : blackPieceToChar.get(this.getPieceType());
    }

    final static Map<PieceType, String> whitePieceToChar = Map.of(
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
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

        return new ArrayList<>();
    }
}
