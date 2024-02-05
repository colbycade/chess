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
    private boolean hasMoved;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
        this.hasMoved = false;
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

    public boolean hasMoved() {
        return hasMoved;
    }


    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
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
    public static Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        var moves = new ArrayList<ChessMove>();
        var myPiece = board.getPiece(myPosition);
        var myRow = myPosition.getRow();
        var myCol = myPosition.getColumn();

        switch (myPiece.getPieceType()) {
            case PAWN -> {
                var direction = myPiece.getTeamColor() == ChessGame.TeamColor.WHITE ? 1 : -1;
                var startingRow = myPiece.getTeamColor() == ChessGame.TeamColor.WHITE ? 2 : 7;
                var endingRow = myPiece.getTeamColor() == ChessGame.TeamColor.WHITE ? 8 : 1;
                // pawns promote to Rook, Knight, Bishop, or Queen (cannot stay a Pawn)
                var promotionTypes = new PieceType[]{PieceType.KNIGHT, PieceType.BISHOP, PieceType.ROOK, PieceType.QUEEN};

                // move one forward
                var one_forward = new ChessPosition(myRow + direction, myCol);
                if (board.squareIsEmpty(one_forward)) {
                    if (one_forward.getRow() == endingRow) {
                        // promote
                        for (PieceType type : promotionTypes) {
                            moves.add(new ChessMove(myPosition, one_forward, type));
                        }
                    } else {
                        // no promotion
                        moves.add(new ChessMove(myPosition, one_forward, null));
                    }

                    // first move can go two forward (but one forward must also be empty)
                    if (myRow == startingRow) {
                        var two_forward = new ChessPosition(myRow + 2 * direction, myCol);
                        if (board.squareIsEmpty(two_forward)) {
                            moves.add(new ChessMove(myPosition, two_forward, null));
                        }
                    }
                }

                // capture left
                var frontLeft = new ChessPosition(myRow + direction, myCol - direction);
                if (frontLeft.isInbounds() && !board.squareIsEmpty(frontLeft) &&
                        board.getPiece(frontLeft).getTeamColor() != myPiece.getTeamColor()) {
                    if (frontLeft.getRow() == endingRow) {
                        // promote
                        for (PieceType type : promotionTypes) {
                            moves.add(new ChessMove(myPosition, frontLeft, type));
                        }
                    } else {
                        // no promotion
                        moves.add(new ChessMove(myPosition, frontLeft, null));
                    }
                }

                // capture right
                var frontRight = new ChessPosition(myRow + direction, myCol + direction);
                if (frontRight.isInbounds() && !board.squareIsEmpty(frontRight) &&
                        board.getPiece(frontRight).getTeamColor() != myPiece.getTeamColor()) {
                    if (frontRight.getRow() == endingRow) {
                        // promote
                        for (PieceType type : promotionTypes) {
                            moves.add(new ChessMove(myPosition, frontRight, type));
                        }
                    } else {
                        // no promotion
                        moves.add(new ChessMove(myPosition, frontRight, null));
                    }
                }

            }

            case BISHOP -> {
                int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}}; // each diagonal

                for (int[] directionPair : directions) {
                    var rowDirection = directionPair[0];
                    var colDirection = directionPair[1];
                    var stepSize = 1;
                    var newPosition = new ChessPosition(myPosition.getRow() + rowDirection * stepSize, myPosition.getColumn() + colDirection * stepSize);
                    while (newPosition.isInbounds()) {
                        ChessPiece pieceAtPosition = board.getPiece(newPosition);
                        // position is unoccupied; add to moves and continue searching this direction
                        if (pieceAtPosition == null) {
                            moves.add(new ChessMove(myPosition, newPosition, null));
                            stepSize++;
                            newPosition = new ChessPosition(myPosition.getRow() + rowDirection * stepSize, myPosition.getColumn() + colDirection * stepSize);
                        }
                        // position is occupied; capture if possible and stop searching this direction
                        else {
                            // add to moves if capture is possible
                            if (pieceAtPosition.getTeamColor() != myPiece.getTeamColor()) {
                                moves.add(new ChessMove(myPosition, newPosition, null));
                            }
                            break;
                        }
                    }
                }
            }

            case KNIGHT -> {
                // all L-shapes
                int[][] possibleSteps = {
                        {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                        {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
                };

                for (int[] stepPair : possibleSteps) {
                    var rowStep = stepPair[0];
                    var colStep = stepPair[1];
                    var newPosition = new ChessPosition(myPosition.getRow() + rowStep, myPosition.getColumn() + colStep);
                    if (newPosition.isInbounds()) {
                        ChessPiece pieceAtPosition = board.getPiece(newPosition);
                        // position is empty or capture is possible
                        if (pieceAtPosition == null || pieceAtPosition.getTeamColor() != myPiece.getTeamColor()) {
                            moves.add(new ChessMove(myPosition, newPosition, null));
                        }
                    }
                }
            }

            case ROOK -> {
                int[][] directions = {{0, 1}, {0, -1}, {-1, 0}, {1, 0}}; // up down left right

                for (int[] directionPair : directions) {
                    var rowDirection = directionPair[0];
                    var colDirection = directionPair[1];
                    var stepSize = 1;
                    var newPosition = new ChessPosition(myPosition.getRow() + rowDirection * stepSize, myPosition.getColumn() + colDirection * stepSize);
                    while (newPosition.isInbounds()) {
                        ChessPiece pieceAtPosition = board.getPiece(newPosition);
                        // position is unoccupied; add to moves and continue searching this direction
                        if (pieceAtPosition == null) {
                            moves.add(new ChessMove(myPosition, newPosition, null));
                            stepSize++;
                            newPosition = new ChessPosition(myPosition.getRow() + rowDirection * stepSize, myPosition.getColumn() + colDirection * stepSize);
                        }
                        // position is occupied; capture if possible and stop searching this direction
                        else {
                            // add to moves if capture is possible
                            if (pieceAtPosition.getTeamColor() != myPiece.getTeamColor()) {
                                moves.add(new ChessMove(myPosition, newPosition, null));
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
                    var rowDirection = directionPair[0];
                    var colDirection = directionPair[1];
                    var stepSize = 1;
                    var newPosition = new ChessPosition(myPosition.getRow() + rowDirection * stepSize, myPosition.getColumn() + colDirection * stepSize);
                    while (newPosition.isInbounds()) {
                        ChessPiece pieceAtPosition = board.getPiece(newPosition);
                        // position is unoccupied; add to moves and continue searching this direction
                        if (pieceAtPosition == null) {
                            moves.add(new ChessMove(myPosition, newPosition, null));
                            stepSize++;
                            newPosition = new ChessPosition(myPosition.getRow() + rowDirection * stepSize, myPosition.getColumn() + colDirection * stepSize);
                        }
                        // position is occupied; capture if possible and stop searching this direction
                        else {
                            // add to moves if capture is possible
                            if (pieceAtPosition.getTeamColor() != myPiece.getTeamColor()) {
                                moves.add(new ChessMove(myPosition, newPosition, null));
                            }
                            break;
                        }
                    }
                }
            }

            case KING -> {
                int[][] possibleSteps = {
                        {0, 1}, {0, -1}, {-1, 0}, {1, 0},   // up down left right
                        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}  // diagonals
                };

                for (int[] stepPair : possibleSteps) {
                    var rowStep = stepPair[0];
                    var colStep = stepPair[1];
                    var newPosition = new ChessPosition(myPosition.getRow() + rowStep, myPosition.getColumn() + colStep);
                    if (newPosition.isInbounds()) {
                        ChessPiece pieceAtPosition = board.getPiece(newPosition);
                        // position is unoccupied; add to moves
                        if (pieceAtPosition == null) {
                            moves.add(new ChessMove(myPosition, newPosition, null));
                        }
                        // position is occupied; capture if possible
                        else if (pieceAtPosition.getTeamColor() != myPiece.getTeamColor()) {
                            moves.add(new ChessMove(myPosition, newPosition, null));
                        }
                    }
                }
            }
        }

        return moves;
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
}
