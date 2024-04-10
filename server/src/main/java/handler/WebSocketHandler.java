package handler;

import com.google.gson.Gson;
import dataAccess.AuthDAO;
import dataAccess.GameDAO;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.GameService;
import webSocketMessages.userCommands.*;

@WebSocket
public class WebSocketHandler {
    
    private final GameService gameService;
    private final Gson gson = new Gson();
    
    public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.gameService = new GameService(authDAO, gameDAO);
    }
    
    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception {
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
        
        // Handle the command based on its type
        switch (command.getCommandType()) {
            case JOIN_PLAYER -> handleJoinPlayerCommand(session, (JoinPlayer) command);
            case JOIN_OBSERVER -> handleJoinObserverCommand(session, (JoinObserver) command);
            case MAKE_MOVE -> handleMakeMoveCommand(session, (MakeMove) command);
            case LEAVE -> handleLeaveCommand(session, (Leave) command);
            case RESIGN -> handleResignCommand(session, (Resign) command);
            default -> sendMessage(session, "Unknown command type");
        }
    }
    
    private void handleJoinPlayerCommand(Session session, JoinPlayer command) {
    }
    
    private void handleJoinObserverCommand(Session session, JoinObserver command) {
    }
    
    private void handleMakeMoveCommand(Session session, MakeMove command) {
    }
    
    private void handleLeaveCommand(Session session, Leave command) {
    }
    
    private void handleResignCommand(Session session, Resign command) {
    }
    
    private void sendMessage(Session session, String message) throws Exception {
        session.getRemote().sendString(message);
    }
}
    
    
