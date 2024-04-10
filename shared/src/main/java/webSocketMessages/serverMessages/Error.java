package webSocketMessages.serverMessages;

public class Error extends ServerMessage {
    private final String errorMessage;
    
    public Error(String message) {
        super(ServerMessageType.ERROR);
        this.errorMessage = message;
    }
}
