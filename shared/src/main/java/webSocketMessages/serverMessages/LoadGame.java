package webSocketMessages.serverMessages;

import model.GameData;

public class LoadGame extends ServerMessage {
    
    private final GameData gameData;
    
    public LoadGame(GameData game) {
        super(ServerMessageType.LOAD_GAME);
        this.gameData = game;
    }
    
    public GameData gameData() {
        return gameData;
    }
}
