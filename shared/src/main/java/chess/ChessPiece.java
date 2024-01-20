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

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
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

    private final static Map<PieceType, String> whitePieceToChar = Map.of(
            PieceType.PAWN, "♙",    // White Pawn
            PieceType.KNIGHT, "♘",  // White Knight
            PieceType.ROOK, "♖",    // White Rook
            PieceType.QUEEN, "♕",   // White Queen
            PieceType.KING, "♔",    // White King
            PieceType.BISHOP, "♗"); // White Bishop

    private final static Map<PieceType, String> blackPieceToChar = Map.of(
            PieceType.PAWN, "♟",    // Black Pawn
            PieceType.KNIGHT, "♞",  // Black Knight
            PieceType.ROOK, "♜",    // Black Rook
            PieceType.QUEEN, "♛",   // Black Queen
            PieceType.KING, "♚",    // Black King
            PieceType.BISHOP, "♝"); // Black Bishop

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        var moves = new ArrayList<ChessMove>();
        ChessPiece piece = board.getPiece(myPosition);
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        switch (piece.getPieceType()) {
            case PAWN:
                int direction = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 1 : -1;
                int startingRow = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 2 : 7;
                int endingRow = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 8 : 1;

                // move one forward
                var one_forward = new ChessPosition(row + direction, col);
                if (board.squareIsEmpty(one_forward)) {
                    if (one_forward.getRow() == endingRow) {
                        // promote to Rook, Knight, Bishop, or Queen (they cannot stay a Pawn)
                        PieceType[] promotions = {PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN};
                        for (PieceType type : promotions) {
                            moves.add(new ChessMove(myPosition, one_forward, type));
                        }
                    } else {
                        // no promotion
                        moves.add(new ChessMove(myPosition, one_forward, null));
                    }

                    // first move can go two forward (but one forward must also be empty)
                    if (row == startingRow) {
                        var two_forward = new ChessPosition(row + 2 * direction, col);
                        if (board.squareIsEmpty(two_forward)) {
                            moves.add(new ChessMove(myPosition, two_forward, null));
                        }
                    }
                }

                // capture left
                var front_left = new ChessPosition(row + direction, col - direction);
                if (isInbounds(front_left) && !board.squareIsEmpty(front_left) &&
                        board.getPiece(front_left).getTeamColor() != piece.getTeamColor()) {
                    if (front_left.getRow() == endingRow) {
                        // promote to Rook, Knight, Bishop, or Queen (they cannot stay a Pawn)
                        PieceType[] promotions = {PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN};
                        for (PieceType type : promotions) {
                            moves.add(new ChessMove(myPosition, front_left, type));
                        }
                    } else {
                        // no promotion
                        moves.add(new ChessMove(myPosition, front_left, null));
                    }
                }

                // capture right
                var front_right = new ChessPosition(row + direction, col + direction);
                if (isInbounds(front_right) && !board.squareIsEmpty(front_right) &&
                        board.getPiece(front_right).getTeamColor() != piece.getTeamColor()) {
                    if (front_right.getRow() == endingRow) {
                        // promote to Rook, Knight, Bishop, or Queen (they cannot stay a Pawn)
                        PieceType[] promotions = {PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN};
                        for (PieceType type : promotions) {
                            moves.add(new ChessMove(myPosition, front_right, type));
                        }
                    } else {
                        // no promotion
                        moves.add(new ChessMove(myPosition, front_right, null));
                    }
                }

                // en passant

                // promotion

            case BISHOP:
                int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}}; // each diagonal
                for (int[] pair : directions) {
                    int rowDirection = pair[0];
                    int colDirection = pair[1];
                    int stepSize = 1;
                    var currPosition = new ChessPosition(myPosition.getRow() + rowDirection * stepSize, myPosition.getColumn() + colDirection * stepSize);
                    while (isInbounds(currPosition)) {
                        ChessPiece pieceAtPosition = board.getPiece(currPosition);
                        // position is unoccupied; add to moves and continue searching this direction
                        if (pieceAtPosition == null) {
                            moves.add(new ChessMove(myPosition, currPosition, null));
                            stepSize++;
                            currPosition = new ChessPosition(myPosition.getRow() + rowDirection * stepSize, myPosition.getColumn() + colDirection * stepSize);
                        }
                        // position is occupied; capture if possible and stop searching this direction
                        else {
                            // add to moves if capture is possible
                            if (pieceAtPosition.getTeamColor() != piece.getTeamColor()) {
                                moves.add(new ChessMove(myPosition, currPosition, null));
                            }
                            break;
                        }
                    }
                }


            case KNIGHT:

            case ROOK:

            case QUEEN:

            case KING:
        }

        return moves;
    }

    private boolean isInbounds(ChessPosition position) {
        int row = position.getRow();
        int col = position.getColumn();
        return (1 <= row) && (row <= 8) && (1 <= col) && (col <= 8);
    }
}
