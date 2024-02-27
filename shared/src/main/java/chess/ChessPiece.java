package chess;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import chess.ChessMovesStrategies.*;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;
    private boolean hasNotMoved;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
        this.hasNotMoved = true;
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

    public boolean hasNotMoved() {
        return hasNotMoved;
    }

    public void setHasNotMoved(boolean hasNotMoved) {
        this.hasNotMoved = hasNotMoved;
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

    private final static Map<PieceType, String> whitePieceToChar = Map.of(
            PieceType.PAWN, "♙",        // White Pawn
            PieceType.KNIGHT, "♘",      // White Knight
            PieceType.ROOK, "♖",        // White Rook
            PieceType.QUEEN, "♕",       // White Queen
            PieceType.KING, "♔",        // White King
            PieceType.BISHOP, "♗");     // White Bishop
    private final static Map<PieceType, String> blackPieceToChar = Map.of(
            PieceType.PAWN, "♟",        // Black Pawn
            PieceType.KNIGHT, "♞",      // Black Knight
            PieceType.ROOK, "♜",        // Black Rook
            PieceType.QUEEN, "♛",       // Black Queen
            PieceType.KING, "♚",        // Black King
            PieceType.BISHOP, "♝");     // Black Bishop

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public static Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        var myPiece = board.getPiece(myPosition);
        PieceMovesStrategy strategy = MovesStrategyFactory.getStrategy(myPiece.getPieceType());
        return strategy.calculateValidMoves(board, myPosition);
    }

}
