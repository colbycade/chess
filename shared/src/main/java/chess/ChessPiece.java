package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

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
        ChessPiece myPiece = board.getPiece(myPosition);
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        switch (myPiece.getPieceType()) {
            case PAWN -> {
                int direction = myPiece.getTeamColor() == ChessGame.TeamColor.WHITE ? 1 : -1;
                int startingRow = myPiece.getTeamColor() == ChessGame.TeamColor.WHITE ? 2 : 7;
                int endingRow = myPiece.getTeamColor() == ChessGame.TeamColor.WHITE ? 8 : 1;

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
                if (front_left.isInbounds() && !board.squareIsEmpty(front_left) &&
                        board.getPiece(front_left).getTeamColor() != myPiece.getTeamColor()) {
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
                if (front_right.isInbounds() && !board.squareIsEmpty(front_right) &&
                        board.getPiece(front_right).getTeamColor() != myPiece.getTeamColor()) {
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

            }

            case BISHOP -> {
                int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}}; // each diagonal

                for (int[] directionPair : directions) {
                    int rowDirection = directionPair[0];
                    int colDirection = directionPair[1];
                    int stepSize = 1;
                    var currPosition = new ChessPosition(myPosition.getRow() + rowDirection * stepSize, myPosition.getColumn() + colDirection * stepSize);
                    while (currPosition.isInbounds()) {
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
                            if (pieceAtPosition.getTeamColor() != myPiece.getTeamColor()) {
                                moves.add(new ChessMove(myPosition, currPosition, null));
                            }
                            break;
                        }
                    }
                }
            }

            case KNIGHT -> {
                int[][] jumps = {   // all L-shapes
                        {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                        {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
                };

                for (int[] offsetPair : jumps) {
                    int rowOffset = offsetPair[0];
                    int colOffset = offsetPair[1];
                    var currPosition = new ChessPosition(myPosition.getRow() + rowOffset, myPosition.getColumn() + colOffset);
                    if (currPosition.isInbounds()) {
                        ChessPiece pieceAtPosition = board.getPiece(currPosition);
                        if (pieceAtPosition == null || pieceAtPosition.getTeamColor() != myPiece.getTeamColor()) {
                            // position is empty or capture is possible
                            moves.add(new ChessMove(myPosition, currPosition, null));
                        }
                    }
                }
            }

            case ROOK -> {
                int[][] directions = {{0, 1}, {0, -1}, {-1, 0}, {1, 0}}; // up down left right

                for (int[] directionPair : directions) {
                    int rowDirection = directionPair[0];
                    int colDirection = directionPair[1];
                    int stepSize = 1;
                    var currPosition = new ChessPosition(myPosition.getRow() + rowDirection * stepSize, myPosition.getColumn() + colDirection * stepSize);
                    while (currPosition.isInbounds()) {
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
                            if (pieceAtPosition.getTeamColor() != myPiece.getTeamColor()) {
                                moves.add(new ChessMove(myPosition, currPosition, null));
                            }
                            break;
                        }
                    }
                }
            }

            case QUEEN -> {
                int[][] directions = {
                        {0, 1}, {0, -1}, {-1, 0}, {1, 0},   // up down left right
                        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}  // diagonals
                };

                for (int[] directionPair : directions) {
                    int rowDirection = directionPair[0];
                    int colDirection = directionPair[1];
                    int stepSize = 1;
                    var currPosition = new ChessPosition(myPosition.getRow() + rowDirection * stepSize, myPosition.getColumn() + colDirection * stepSize);
                    while (currPosition.isInbounds()) {
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
                            if (pieceAtPosition.getTeamColor() != myPiece.getTeamColor()) {
                                moves.add(new ChessMove(myPosition, currPosition, null));
                            }
                            break;
                        }
                    }
                }
            }

            case KING -> {
                int[][] directions = {
                        {0, 1}, {0, -1}, {-1, 0}, {1, 0},   // up down left right
                        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}  // diagonals
                };

                for (int[] offsetPair : directions) {
                    int rowOffset = offsetPair[0];
                    int colOffset = offsetPair[1];
                    var currPosition = new ChessPosition(myPosition.getRow() + rowOffset, myPosition.getColumn() + colOffset);
                    if (currPosition.isInbounds()) {
                        ChessPiece pieceAtPosition = board.getPiece(currPosition);
                        // position is unoccupied; add to moves
                        if (pieceAtPosition == null) {
                            moves.add(new ChessMove(myPosition, currPosition, null));
                        }
                        // position is occupied; capture if possible
                        else if (pieceAtPosition.getTeamColor() != myPiece.getTeamColor()) {
                            moves.add(new ChessMove(myPosition, currPosition, null));
                        }
                    }
                }
            }
        }

        return moves;
    }
}
