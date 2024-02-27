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
    private ChessMove lastMove;

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
            if (wouldNotBeInCheck(ChessMove)) allValidMoves.add(ChessMove);
        }

        // en passant
        System.out.println(board);
        var piece = board.getPiece(startPosition);
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            var direction = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 1 : -1;
            // check left to en passant front left
            var left = new ChessPosition(startPosition.getRow(), startPosition.getColumn() - direction);
            var frontLeft = new ChessPosition(startPosition.getRow() + direction, startPosition.getColumn() - direction);
            if (left.isInbounds() && !board.squareIsEmpty(left) &&
                    board.getPiece(left).getTeamColor() != piece.getTeamColor() && canBeEnPassant(left)) {
                allValidMoves.add(new ChessMove(startPosition, frontLeft, null));
            }

            // check right to en passant front right
            var right = new ChessPosition(startPosition.getRow(), startPosition.getColumn() + direction);
            var frontRight = new ChessPosition(startPosition.getRow() + direction, startPosition.getColumn() + direction);
            if (right.isInbounds() && !board.squareIsEmpty(right) &&
                    board.getPiece(right).getTeamColor() != piece.getTeamColor() && canBeEnPassant(right)) {
                allValidMoves.add(new ChessMove(startPosition, frontRight, null));
            }
        }

        // castle if both king and rook are unmoved and path is clear between them
        if (piece.getPieceType() == ChessPiece.PieceType.KING && piece.hasNotMoved()) {
            // kingside castle (h rook)
            var rookPosition = new ChessPosition(startPosition.getRow(), 8);
            if (!board.squareIsEmpty(rookPosition) && board.getPiece(rookPosition).hasNotMoved()
                    && isPathClearToCastle(startPosition, rookPosition)) {
                allValidMoves.add(new ChessMove(startPosition, new ChessPosition(startPosition.getRow(), 7), null));
            }

            // queenside castle (a rook)
            rookPosition = new ChessPosition(startPosition.getRow(), 1);
            if (!board.squareIsEmpty(rookPosition) && board.getPiece(rookPosition).hasNotMoved()
                    && isPathClearToCastle(startPosition, rookPosition)) {
                allValidMoves.add(new ChessMove(startPosition, new ChessPosition(startPosition.getRow(), 3), null));
            }
        }

        System.out.printf("valid moves: %s%n", allValidMoves);
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
        if (!validMoves(move.getStartPosition()).contains(move)) {
            throw new InvalidMoveException("Invalid move");
        }

        var piece = board.getPiece(move.getStartPosition());

        // if en passant, remove other pawn
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN && move.getStartPosition().getColumn() != move.getEndPosition().getColumn()
                && board.squareIsEmpty(move.getEndPosition())) { // move diagonal but no pawn diagonal, must be en passant
            var direction = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 1 : -1;
            board.removePiece(new ChessPosition(move.getEndPosition().getRow() - direction, move.getEndPosition().getColumn()));
        }

        // if castling, move rook
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            var difference = move.getEndPosition().getColumn() - move.getStartPosition().getColumn();
            // kingside castle (king goes right)
            if (difference == 2) {
                var rookStartPosition = new ChessPosition(move.getStartPosition().getRow(), 8);
                var rookEndPosition = new ChessPosition(move.getStartPosition().getRow(), 6);
                executeMove(new ChessMove(rookStartPosition, rookEndPosition, null));
            } // queenside castle (king goes left)
            else if (difference == -2) {
                var rookStartPosition = new ChessPosition(move.getStartPosition().getRow(), 1);
                var rookEndPosition = new ChessPosition(move.getStartPosition().getRow(), 4);
                executeMove(new ChessMove(rookStartPosition, rookEndPosition, null));
            }
        }

        // make the actual move
        executeMove(move);

        // update that piece has moved
        piece.setHasNotMoved(false);

        // save previous move
        lastMove = move;

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
                        if (wouldNotBeInCheck(validMove)) return false;  // check if still in check
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

    private boolean wouldNotBeInCheck(ChessMove move) {
        var teamColor = board.getPiece(move.getStartPosition()).getTeamColor();
        var testGame = new ChessGame(new ChessBoard(board));

        // simulate move then check if in check
        testGame.executeMove(move);

        return !testGame.isInCheck(teamColor);
    }

    private boolean canBeEnPassant(ChessPosition position) {
        var piece = board.getPiece(position);
        // must be a pawn
        if (piece.getPieceType() != ChessPiece.PieceType.PAWN) return false;
        // must have been last move
        if (lastMove == null || !position.equals(lastMove.getEndPosition())) return false;
        // must have been a double move
        var diff = Math.abs(lastMove.getEndPosition().getRow() - lastMove.getStartPosition().getRow());
        return (diff == 2);
    }

    /**
     * Function to determine if the path is clear for the king to castle
     * meaning there are no pieces between king and rook and the king would not undergo check
     *
     * @param kingPosition position of king
     * @param rookPosition position of selected rook
     * @return true if there are no pieces between the king and rook
     */
    private boolean isPathClearToCastle(ChessPosition kingPosition, ChessPosition rookPosition) {
        var leftCol = Math.min(kingPosition.getColumn(), rookPosition.getColumn());
        int rightCol = Math.max(kingPosition.getColumn(), rookPosition.getColumn());

        // check each square between the king and the rook
        for (int col = leftCol + 1; col < rightCol; col++) {
            var currPosition = new ChessPosition(kingPosition.getRow(), col);
            if (!board.squareIsEmpty(currPosition) || !wouldNotBeInCheck(new ChessMove(kingPosition, currPosition, null))) {
                return false; // found a piece in the way or was checked
            }
        }
        return true;
    }

    private void executeMove(ChessMove move) {
        // remove piece from previous position
        var piece = this.getBoard().getPiece(move.getStartPosition());
        board.removePiece(move.getStartPosition());

        // check for promotion
        if (move.getPromotionPiece() != null) {
            piece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
        }

        // add piece to new position
        board.addPiece(move.getEndPosition(), piece);
    }


}