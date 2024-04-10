package webSocketMessages.userCommands;

import chess.ChessGame;

public class JoinPlayer extends UserGameCommand {
    
    private final Integer gameID;
    private final ChessGame.TeamColor playerColor;
    
    public JoinPlayer(String authToken, Integer gameID, ChessGame.TeamColor playerColor) {
        super(authToken);
        this.commandType = CommandType.JOIN_PLAYER;
        this.gameID = gameID;
        this.playerColor = playerColor;
    }
    
    public Integer gameID() {
        return gameID;
    }
    
    public ChessGame.TeamColor playerColor() {
        return playerColor;
    }
}
