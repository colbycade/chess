package chess;

import java.util.ArrayList;
import java.util.Collection;

public class ChessMovesStrategies {
    public class MovesStrategyFactory {
        public static PieceMovesStrategy getStrategy(ChessPiece.PieceType type) {
            return switch (type) {
                case PAWN -> new PawnMovesStrategy();
                case KNIGHT -> new KnightMovesStrategy();
                case BISHOP -> new BishopMovesStrategy();
                case ROOK -> new RookMovesStrategy();
                case QUEEN -> new QueenMovesStrategy();
                case KING -> new KingMovesStrategy();
            };
        }
    }

    public interface PieceMovesStrategy {
        Collection<ChessMove> calculateValidMoves(ChessBoard board, ChessPosition position);
    }

    private static class BishopMovesStrategy implements PieceMovesStrategy {

        @Override
        public Collection<ChessMove> calculateValidMoves(ChessBoard board, ChessPosition myPosition) {
            int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}}; // diagonals only

            return calculateDirectionalMoves(board, myPosition, directions);
        }
    }

    private static class RookMovesStrategy implements PieceMovesStrategy {

        @Override
        public Collection<ChessMove> calculateValidMoves(ChessBoard board, ChessPosition myPosition) {
            int[][] directions = {{0, 1}, {0, -1}, {-1, 0}, {1, 0}}; // up down left right

            return calculateDirectionalMoves(board, myPosition, directions);
        }
    }

    private static class QueenMovesStrategy implements PieceMovesStrategy {

        @Override
        public Collection<ChessMove> calculateValidMoves(ChessBoard board, ChessPosition myPosition) {
            int[][] directions = {
                    {0, 1}, {0, -1}, {-1, 0}, {1, 0},   // up down left right
                    {1, 1}, {1, -1}, {-1, 1}, {-1, -1}  // diagonals
            };

            return calculateDirectionalMoves(board, myPosition, directions);
        }
    }


    private static Collection<ChessMove> calculateDirectionalMoves(ChessBoard board, ChessPosition myPosition, int[][] directions) {
        var moves = new ArrayList<ChessMove>();
        var myPiece = board.getPiece(myPosition);
        var myRow = myPosition.getRow();
        var myCol = myPosition.getColumn();

        for (int[] direction : directions) {
            int rowDirection = direction[0];
            int colDirection = direction[1];
            int stepSize = 1;
            ChessPosition newPosition = new ChessPosition(myRow + rowDirection * stepSize, myCol + colDirection * stepSize);

            while (newPosition.isInbounds()) {
                ChessPiece pieceAtPosition = board.getPiece(newPosition);
                if (pieceAtPosition == null) {  // position is unoccupied
                    moves.add(new ChessMove(myPosition, newPosition, null));
                    stepSize++;
                    newPosition = new ChessPosition(myRow + rowDirection * stepSize, myCol + colDirection * stepSize);
                } else {    // position is occupied; capture if possible
                    if (pieceAtPosition.getTeamColor() != myPiece.getTeamColor()) { // capture if enemy
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    }
                    break;  // stop searching this direction
                }
            }
        }

        return moves;
    }

    private static class KingMovesStrategy implements PieceMovesStrategy {

        @Override
        public Collection<ChessMove> calculateValidMoves(ChessBoard board, ChessPosition myPosition) {
            var moves = new ArrayList<ChessMove>();
            var myPiece = board.getPiece(myPosition);

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

            return moves;
        }
    }

    private static class KnightMovesStrategy implements PieceMovesStrategy {

        @Override
        public Collection<ChessMove> calculateValidMoves(ChessBoard board, ChessPosition myPosition) {
            var moves = new ArrayList<ChessMove>();
            var myPiece = board.getPiece(myPosition);

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

            return moves;
        }
    }

    private static class PawnMovesStrategy implements PieceMovesStrategy {

        @Override
        public Collection<ChessMove> calculateValidMoves(ChessBoard board, ChessPosition myPosition) {
            var moves = new ArrayList<ChessMove>();
            var myPiece = board.getPiece(myPosition);
            var myRow = myPosition.getRow();
            var myCol = myPosition.getColumn();

            var direction = myPiece.getTeamColor() == ChessGame.TeamColor.WHITE ? 1 : -1;
            var startingRow = myPiece.getTeamColor() == ChessGame.TeamColor.WHITE ? 2 : 7;
            var endingRow = myPiece.getTeamColor() == ChessGame.TeamColor.WHITE ? 8 : 1;
            // pawns promote to Rook, Knight, Bishop, or Queen (cannot stay a Pawn)
            var promotionTypes = new ChessPiece.PieceType[]{ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.ROOK, ChessPiece.PieceType.QUEEN};

            // move one forward
            var one_forward = new ChessPosition(myRow + direction, myCol);
            if (board.squareIsEmpty(one_forward)) {
                if (one_forward.getRow() == endingRow) {
                    // promote
                    for (ChessPiece.PieceType type : promotionTypes) {
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
                    for (ChessPiece.PieceType type : promotionTypes) {
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
                    for (ChessPiece.PieceType type : promotionTypes) {
                        moves.add(new ChessMove(myPosition, frontRight, type));
                    }
                } else {
                    // no promotion
                    moves.add(new ChessMove(myPosition, frontRight, null));
                }
            }

            return moves;
        }
    }
}
