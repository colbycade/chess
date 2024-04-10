package webSocketMessages.userCommands;

public class Leave extends UserGameCommand {
    
    private Integer gameID;
    
    public Leave(String authToken, Integer gameID) {
        super(authToken);
        this.commandType = CommandType.LEAVE;
        this.gameID = gameID;
    }
}