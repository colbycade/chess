package webSocketMessages.userCommands;

import chess.ChessMove;

public class MakeMove extends UserGameCommand {
    
    private final Integer gameID;
    private final ChessMove move;
    
    public MakeMove(String authToken, Integer gameID, ChessMove move) {
        super(authToken);
        this.commandType = CommandType.MAKE_MOVE;
        this.gameID = gameID;
        this.move = move;
    }
    
    public Integer gameID() {
        return gameID;
    }
    
    public ChessMove move() {
        return move;
    }
}
