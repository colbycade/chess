package websocket.commands;

public class Leave extends UserGameCommand {
    
    private final Integer gameID;
    
    public Leave(String authToken, Integer gameID) {
        super(authToken);
        this.commandType = CommandType.LEAVE;
        this.gameID = gameID;
    }
    
    public Integer gameID() {
        return gameID;
    }
}