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
    private TeamColor winner;
    
    public ChessGame() {
        ChessBoard startingBoard = new ChessBoard();
        startingBoard.resetBoard(); // set to starting position
        board = startingBoard;
        teamColor = TeamColor.WHITE;
        winner = null;
    }
    
    public ChessGame(ChessBoard startingBoard) {
        board = startingBoard;
    }
    
    public ChessBoard getBoard() {
        return board;
    }
    
    public void setBoard(ChessBoard board) {
        this.board = board;
    }
    
    public TeamColor getTeamTurn() {
        return teamColor;
    }
    
    public void setTeamTurn(TeamColor team) {
        teamColor = team;
    }
    
    public TeamColor getWinner() {
        return winner;
    }
    
    public void setWinner(TeamColor winner) {
        this.winner = winner;
    }
    
    public enum TeamColor {
        WHITE,
        BLACK,
        DRAW
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
        var piece = board.getPiece(startPosition);
        
        // add standard moves
        var potentialMoves = ChessPiece.pieceMoves(board, startPosition);
        for (var chessMove : potentialMoves) {
            if (!wouldBeInCheck(chessMove)) allValidMoves.add(chessMove);
        }
        
        // add en passant moves for pawns
        if (piece != null && piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            int direction = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 1 : -1;
            allValidMoves.addAll(getEnPassantMoves(startPosition, direction));
        }
        
        // add castling moves for king if it has not moved
        if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.hasNotMoved()) {
            allValidMoves.addAll(getKingCastlingMoves(startPosition));
        }
        
        return allValidMoves;
    }
    
    // Check for En Passant on both sides of the pawn
    private Collection<ChessMove> getEnPassantMoves(ChessPosition startPosition, int direction) {
        var moves = new ArrayList<ChessMove>();
        
        // Column offsets for left and right checks
        int[] columnOffsets = {-1, 1};
        for (int offset : columnOffsets) {
            ChessPosition adjacentPosition = new ChessPosition(startPosition.getRow(), startPosition.getColumn() + offset);
            ChessPosition enPassantTarget = new ChessPosition(startPosition.getRow() + direction, startPosition.getColumn() + offset);
            
            if (adjacentPosition.isInbounds() && canBeEnPassant(adjacentPosition)) {
                moves.add(new ChessMove(startPosition, enPassantTarget, null));
            }
        }
        return moves;
    }
    
    // Check if the pawn at the given position can be captured by en passant
    private boolean canBeEnPassant(ChessPosition position) {
        var piece = board.getPiece(position);
        // must be a pawn
        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.PAWN) return false;
        // must have been last move
        if (lastMove == null || !position.equals(lastMove.getEndPosition())) return false;
        // must have been a double move
        var diff = Math.abs(lastMove.getEndPosition().getRow() - lastMove.getStartPosition().getRow());
        return (diff == 2);
    }
    
    // Get valid castling moves for a king
    private Collection<ChessMove> getKingCastlingMoves(ChessPosition kingStart) {
        var moves = new ArrayList<ChessMove>();
        var king = board.getPiece(kingStart);
        if (king == null || king.getPieceType() != ChessPiece.PieceType.KING) return moves;
        
        // King must be at its starting position
        if ((king.getTeamColor() == TeamColor.WHITE && !kingStart.equals(new ChessPosition(1, 5))) ||
                (king.getTeamColor() == TeamColor.BLACK && !kingStart.equals(new ChessPosition(8, 5)))) {
            return moves;
        }
        
        // King must not have moved
        if (!king.hasNotMoved()) return moves;
        
        // [kingEndColumn, rookColumn]
        int[][] castlingPositions = {
                {7, 8}, // Kingside (h rook)
                {3, 1}  // Queenside (a rook)
        };
        
        for (int[] rookAndKingColumns : castlingPositions) {
            var kingEnd = new ChessPosition(kingStart.getRow(), rookAndKingColumns[0]);
            var rookPos = new ChessPosition(kingStart.getRow(), rookAndKingColumns[1]);
            var rook = board.getPiece(rookPos);
            
            // rook present, same color, and hasn't moved
            if (rook == null ||
                    rook.getPieceType() != ChessPiece.PieceType.ROOK ||
                    rook.getTeamColor() != king.getTeamColor() ||
                    !rook.hasNotMoved()) continue;
            
            // clear path between king and rook
            if (!isPathClearToCastle(kingStart, rookPos)) continue;
            
            moves.add(new ChessMove(kingStart, kingEnd, null));
        }
        return moves;
    }
    
    // Check if path is clear between king and rook
    private boolean isPathClearToCastle(ChessPosition kingPosition, ChessPosition rookPosition) {
        var leftCol = Math.min(kingPosition.getColumn(), rookPosition.getColumn());
        int rightCol = Math.max(kingPosition.getColumn(), rookPosition.getColumn());
        
        // check each square between the king and the rook
        for (int col = leftCol + 1; col < rightCol; col++) {
            var currPosition = new ChessPosition(kingPosition.getRow(), col);
            if (!board.squareIsEmpty(currPosition) || wouldBeInCheck(new ChessMove(kingPosition, currPosition, null))) {
                return false; // found a piece in the way or was checked
            }
        }
        return true;
    }
    
    
    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform (includes start and end positions)
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        // must be a piece at start position
        var startPos = move.getStartPosition();
        var piece = board.getPiece(startPos);
        if (piece == null) {
            throw new InvalidMoveException("Invalid move: No piece at start position");
        }
        
        // can't make move out of turn
        if (piece.getTeamColor() != getTeamTurn()) {
            throw new InvalidMoveException("Invalid move: Out of turn");
        }
        
        // verify that move is valid
        if (!validMoves(startPos).contains(move)) {
            throw new InvalidMoveException("Invalid move");
        }
        
        // make the move
        executeMove(move);
        
        // update that piece has moved
        piece.setHasNotMoved(false);
        
        // save previous move
        lastMove = move;
        
        // switch turns
        setTeamTurn(getTeamTurn() == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
        
        // check for checkmate or stalemate
        if (isInCheckmate(getTeamTurn())) {
            winner = getTeamTurn() == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
        } else if (isInStalemate(getTeamTurn())) {
            winner = TeamColor.DRAW;
        }
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
        
        // King is in checkmate if currently in check and all of his side's potential moves are invalid
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                var currPosition = new ChessPosition(row, col);
                if (board.getPiece(currPosition) != null && board.getPiece(currPosition).getTeamColor() == teamColor) {
                    if (!validMoves(currPosition).isEmpty()) {
                        return false;  // found a valid move, so not checkmate
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
    
    // Find the position of the king
    private ChessPosition findKing(TeamColor teamColor) {
        ChessPosition kingPosition = null;
        
        outerLoop:
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                var currPosition = new ChessPosition(row, col);
                var currPiece = board.getPiece(currPosition);
                if ((currPiece != null) && (currPiece.getTeamColor() == teamColor)
                        && (currPiece.getPieceType() == ChessPiece.PieceType.KING)) {
                    kingPosition = currPosition;
                    break outerLoop;
                }
            }
        }
        return kingPosition;
    }
    
    // Check if a move would not put the team in check
    private boolean wouldBeInCheck(ChessMove move) {
        var teamColor = board.getPiece(move.getStartPosition()).getTeamColor();
        var testGame = new ChessGame(new ChessBoard(board));
        testGame.executeMove(move); // simulate move
        return testGame.isInCheck(teamColor); // check if in check
    }
    
    // Move piece on board, applying castling/en-passant rules if needed (but no check validation)
    private void executeMove(ChessMove move) {
        var piece = this.getBoard().getPiece(move.getStartPosition());
        
        // if en passant, remove other pawn
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN && move.getStartPosition().getColumn() != move.getEndPosition().getColumn()
                && board.squareIsEmpty(move.getEndPosition())) { // move diagonal but no pawn diagonal, must be en passant
            var direction = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 1 : -1;
            var otherPawnPosition = new ChessPosition(move.getEndPosition().getRow() - direction,
                    move.getEndPosition().getColumn());
            board.clearPosition(otherPawnPosition);
        }
        
        // if castling, move rook
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            var difference = move.getEndPosition().getColumn() - move.getStartPosition().getColumn();
            // kingside castle (king goes right)
            if (difference == 2) {
                var rookStartPosition = new ChessPosition(move.getStartPosition().getRow(), 8);
                var rookEndPosition = new ChessPosition(move.getStartPosition().getRow(), 6);
                movePieceOnBoard(new ChessMove(rookStartPosition, rookEndPosition, null));
            } // queenside castle (king goes left)
            else if (difference == -2) {
                var rookStartPosition = new ChessPosition(move.getStartPosition().getRow(), 1);
                var rookEndPosition = new ChessPosition(move.getStartPosition().getRow(), 4);
                movePieceOnBoard(new ChessMove(rookStartPosition, rookEndPosition, null));
            }
        }
        movePieceOnBoard(move);
    }
    
    private void movePieceOnBoard(ChessMove move) {
        // remove piece from previous position
        var piece = this.getBoard().getPiece(move.getStartPosition());
        board.clearPosition(move.getStartPosition());
        // check for promotion
        if (move.getPromotionPiece() != null) {
            piece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
        }
        // add piece to new position
        board.addPiece(move.getEndPosition(), piece);
    }
    
}