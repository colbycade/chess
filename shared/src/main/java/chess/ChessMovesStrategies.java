package chess;

import java.util.ArrayList;
import java.util.Collection;

public class ChessMovesStrategies {

    public static PieceMovesStrategy getPieceStrategy(ChessPiece.PieceType type) {
        return switch (type) {
            case PAWN -> new PawnMovesStrategy();
            case KNIGHT -> new KnightMovesStrategy();
            case BISHOP -> new BishopMovesStrategy();
            case ROOK -> new RookMovesStrategy();
            case QUEEN -> new QueenMovesStrategy();
            case KING -> new KingMovesStrategy();
        };
    }

    public interface PieceMovesStrategy {
        Collection<ChessMove> calculateValidMoves(ChessBoard board, ChessPosition position);
    }

    private static class PawnMovesStrategy implements PieceMovesStrategy {

        // Pawns promote to Rook, Knight, Bishop, or Queen (cannot stay a Pawn)
        private static final ChessPiece.PieceType[] PROMOTION_TYPES = {
                ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.ROOK, ChessPiece.PieceType.QUEEN
        };
        private final ArrayList<ChessMove> moves = new ArrayList<>();
        private ChessBoard board;
        private int startingRow;
        private int endingRow;

        @Override
        public Collection<ChessMove> calculateValidMoves(ChessBoard board, ChessPosition position) {
            this.board = board;
            ChessPiece piece = board.getPiece(position);
            int myRow = position.getRow();
            int myCol = position.getColumn();

            int direction = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 1 : -1;
            startingRow = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 2 : 7;
            endingRow = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 8 : 1;

            // Move one forward
            var one_forward = new ChessPosition(myRow + direction, myCol);
            addMoveIfValid(position, one_forward, true);

            // Capture moves: left and right
            int[] captureDirections = {-1, 1};
            for (int captureDirection : captureDirections) {
                ChessPosition capturePosition = new ChessPosition(myRow + direction, myCol + captureDirection);
                if (capturePosition.isInbounds() && !board.squareIsEmpty(capturePosition) &&
                        board.getPiece(capturePosition).getTeamColor() != piece.getTeamColor()) {
                    addMoveIfValid(position, capturePosition, false);
                }
            }

            return moves;
        }

        private void addMoveIfValid(ChessPosition from, ChessPosition to, boolean checkTwoForward) {
            if (to.isInbounds() && (board.squareIsEmpty(to) || !checkTwoForward)) {
                if (to.getRow() == endingRow) {
                    // Promotion
                    for (ChessPiece.PieceType type : PROMOTION_TYPES) {
                        moves.add(new ChessMove(from, to, type));
                    }
                } else {
                    // Regular move
                    moves.add(new ChessMove(from, to, null));
                    if (checkTwoForward && from.getRow() == startingRow) {
                        // Check for two squares forward on first move
                        ChessPosition twoForward = new ChessPosition(from.getRow() + 2 * (endingRow == 8 ? 1 : -1), from.getColumn());
                        if (board.squareIsEmpty(twoForward)) {
                            moves.add(new ChessMove(from, twoForward, null));
                        }
                    }
                }
            }
        }
    }


    private static class BishopMovesStrategy implements PieceMovesStrategy {
        private static final int[][] DIRECTIONS = {
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}  // diagonals only
        };

        @Override
        public Collection<ChessMove> calculateValidMoves(ChessBoard board, ChessPosition myPosition) {
            return calculateDirectionalMoves(board, myPosition, DIRECTIONS);
        }
    }

    private static class RookMovesStrategy implements PieceMovesStrategy {
        private static final int[][] DIRECTIONS = {
                {0, 1}, {0, -1}, {-1, 0}, {1, 0}    // up down left right
        };

        @Override
        public Collection<ChessMove> calculateValidMoves(ChessBoard board, ChessPosition myPosition) {
            return calculateDirectionalMoves(board, myPosition, DIRECTIONS);
        }
    }


    private static class QueenMovesStrategy implements PieceMovesStrategy {
        private static final int[][] DIRECTIONS = {
                {0, 1}, {0, -1}, {-1, 0}, {1, 0},   // up down left right
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}  // diagonals
        };

        @Override
        public Collection<ChessMove> calculateValidMoves(ChessBoard board, ChessPosition myPosition) {
            return calculateDirectionalMoves(board, myPosition, DIRECTIONS);
        }
    }


    private static class KingMovesStrategy implements PieceMovesStrategy {
        private static final int[][] POSSIBLE_STEPS = {
                {0, 1}, {0, -1}, {-1, 0}, {1, 0},   // up down left right
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}  // diagonals
        };

        @Override
        public Collection<ChessMove> calculateValidMoves(ChessBoard board, ChessPosition myPosition) {
            return calculateStepMoves(board, myPosition, POSSIBLE_STEPS);
        }
    }


    private static class KnightMovesStrategy implements PieceMovesStrategy {
        private static final int[][] POSSIBLE_STEPS = {   // all L-shapes
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };

        @Override
        public Collection<ChessMove> calculateValidMoves(ChessBoard board, ChessPosition myPosition) {
            return calculateStepMoves(board, myPosition, POSSIBLE_STEPS);
        }
    }


    private static Collection<ChessMove> calculateDirectionalMoves(ChessBoard board, ChessPosition position, int[][] directions) {
        var moves = new ArrayList<ChessMove>();
        var piece = board.getPiece(position);
        var row = position.getRow();
        var col = position.getColumn();

        for (int[] direction : directions) {
            int rowDirection = direction[0];
            int colDirection = direction[1];
            int stepSize = 1;
            ChessPosition newPosition = new ChessPosition(row + rowDirection * stepSize, col + colDirection * stepSize);

            while (newPosition.isInbounds()) {
                ChessPiece pieceAtPosition = board.getPiece(newPosition);
                // If the position is occupied, add to moves and continue searching in this direction
                if (pieceAtPosition == null) {
                    moves.add(new ChessMove(position, newPosition, null));
                    stepSize++;
                    newPosition = new ChessPosition(row + rowDirection * stepSize, col + colDirection * stepSize);
                }
                // If the position is occupied by an enemy piece check for enemy and stop searching in this direction
                else {
                    // Add to moves if a capture is possible
                    if (pieceAtPosition.getTeamColor() != piece.getTeamColor()) {
                        moves.add(new ChessMove(position, newPosition, null));
                    }
                    break;
                }
            }
        }

        return moves;
    }

    public static Collection<ChessMove> calculateStepMoves(ChessBoard board, ChessPosition position, int[][] possibleSteps) {
        var moves = new ArrayList<ChessMove>();
        var piece = board.getPiece(position);

        for (int[] step : possibleSteps) {
            int newRow = position.getRow() + step[0];
            int newColumn = position.getColumn() + step[1];
            ChessPosition newPosition = new ChessPosition(newRow, newColumn);
            if (newPosition.isInbounds()) {
                ChessPiece pieceAtPosition = board.getPiece(newPosition);
                // If the position is unoccupied or occupied by an opponent's piece, add to moves
                if (pieceAtPosition == null || pieceAtPosition.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(position, newPosition, null));
                }
            }
        }
        return moves;
    }
}
