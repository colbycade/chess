package webSocketMessages.serverMessages;

import model.GameData;

public class LoadGame extends ServerMessage {
    
    private final GameData game;
    
    public LoadGame(GameData game) {
        super(ServerMessageType.LOAD_GAME);
        this.game = game;
    }
    
    public GameData game() {
        return game;
    }
}
