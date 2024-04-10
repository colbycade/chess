package webSocketMessages.userCommands;

public class Resign extends UserGameCommand {
    
    private final Integer gameID;
    
    public Resign(String authToken, Integer gameID) {
        super(authToken);
        this.commandType = CommandType.RESIGN;
        this.gameID = gameID;
    }
    
    public Integer gameID() {
        return gameID;
    }
}