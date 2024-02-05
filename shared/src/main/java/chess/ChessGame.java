package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor teamColor;

    public ChessGame() {
    }

    public ChessGame(ChessBoard startingBoard) {
        board = startingBoard;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamColor;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamColor = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location (includes checks)
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        var allValidMoves = new ArrayList<ChessMove>();
        for (var ChessMove : ChessPiece.pieceMoves(board, startPosition)) {
            if (canEscapeFromCheck(ChessMove)) allValidMoves.add(ChessMove);
        }
        return allValidMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        // can't make move out of turn
        if (board.getPiece(move.getStartPosition()).getTeamColor() != getTeamTurn()) {
            System.out.println("Error: move out of turn");
            throw new InvalidMoveException("Invalid move: Out of turn");
        }

        // verify that move is valid
        System.out.println(board);
        System.out.printf("Piece at %s%n", move.getStartPosition());
        System.out.printf("suggested move: %s%n", move);
        System.out.printf("open moves: %s%n", ChessPiece.pieceMoves(board, move.getStartPosition()));
        System.out.printf("valid moves: %s%n", validMoves(move.getStartPosition()));

        if (!validMoves(move.getStartPosition()).contains(move)) {
            System.out.println("Error: invalid move provided");
            throw new InvalidMoveException("Invalid move");
        }

        System.out.println("move valid. executing...");

        // remove piece from previous position
        var piece = board.getPiece(move.getStartPosition());
        board.removePiece(move.getStartPosition());

        // add piece to new position
        if (move.getPromotionPiece() == null) {  // no promotion
            board.addPiece(move.getEndPosition(), piece);
        } else {  // promote upon making move
            board.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
        }

        // switch turns
        setTeamTurn(getTeamTurn() == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        // Find king's current position
        var kingPosition = findKing(teamColor);

        // If king not on board, not in check
        if (kingPosition == null) return false;

        // Check all valid moves of enemy pieces to see if any can attack King
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                var currPosition = new ChessPosition(row, col);
                var currPiece = board.getPiece(currPosition);
                if ((currPiece != null) && (currPiece.getTeamColor() != teamColor)) {
                    for (ChessMove availableMove : ChessPiece.pieceMoves(board, currPosition)) {
                        if (availableMove.getEndPosition().equals(kingPosition))
                            return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        // Can't be in checkmate if not currently in check
        if (!isInCheck(teamColor)) return false;

        // King is in checkmate if currently in check and all of his side's potential moves are also in check
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                var currPosition = new ChessPosition(row, col);
                if (board.getPiece(currPosition) != null && board.getPiece(currPosition).getTeamColor() == teamColor) {  // check moves for all pieces of same color
                    for (ChessMove validMove : validMoves(currPosition)) {
                        if (canEscapeFromCheck(validMove)) return false;  // check if still in check
                    }
                }
            }
        }
        return true;  // no moves took king out of check
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                var currPosition = new ChessPosition(row, col);
                var currPiece = board.getPiece(currPosition);
                if ((currPiece != null) && (currPiece.getTeamColor() == teamColor)
                        && !validMoves(currPosition).isEmpty()) return false;
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    private ChessPosition findKing(TeamColor teamColor) {
        ChessPosition kingPosition = null;

        outerLoop:
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                var currPosition = new ChessPosition(row, col);
                var currPiece = board.getPiece(currPosition);
                if ((currPiece != null) &&
                        (currPiece.getTeamColor() == teamColor) && (currPiece.getPieceType() == ChessPiece.PieceType.KING)) {
                    kingPosition = currPosition;
                    break outerLoop;
                }
            }
        }
        return kingPosition;
    }

    private boolean canEscapeFromCheck(ChessMove move) {
        var teamColor = board.getPiece(move.getStartPosition()).getTeamColor();
        var testGame = new ChessGame(new ChessBoard(board));

        // simulate move then check if in check
        // remove piece from previous position
        var piece = testGame.getBoard().getPiece(move.getStartPosition());
        testGame.getBoard().removePiece(move.getStartPosition());

        // add piece to new position
        if (move.getPromotionPiece() == null) {  // no promotion
            testGame.getBoard().addPiece(move.getEndPosition(), piece);
        } else {  // promote upon making move
            testGame.getBoard().addPiece(move.getEndPosition(), new ChessPiece(teamColor, move.getPromotionPiece()));
        }

        return !testGame.isInCheck(teamColor);
    }


}