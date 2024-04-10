package webSocketMessages.userCommands;

public class MakeMove extends UserGameCommand {
    
    private Integer gameID;
    private String move;
    
    public MakeMove(String authToken, Integer gameID, String move) {
        super(authToken);
        this.commandType = CommandType.MAKE_MOVE;
        this.gameID = gameID;
        this.move = move;
    }
}
