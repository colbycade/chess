package handler;

import com.google.gson.Gson;
import dataAccess.AuthDAO;
import dataAccess.GameDAO;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.GameService;
import webSocketMessages.userCommands.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {
    
    private final GameService gameService;
    private final Gson gson = new Gson();
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(UserGameCommand.class, new UserGameCommandDeserializer())
            .create();
    private Map<Integer, Set<Session>> gameSessions;    // Map of game IDs to the set of sessions in the game
    
    public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.gameService = new GameService(authDAO, gameDAO);
    }
    
    @OnWebSocketConnect
    public void onConnect(Session session) {
        // Handle new WebSocket connection
        System.out.println("WebSocket connection opened: " + session.getRemoteAddress());
        gameSessions = new ConcurrentHashMap<>();
    }
    
    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        // Handle WebSocket connection close
        System.out.println("WebSocket connection closed: " + session.getRemoteAddress());
        System.out.println("Reason: " + reason);
        // Remove the session from the game it was in
        gameSessions.values().forEach(sessions -> sessions.remove(session));
    }
    
    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception {
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
        System.out.println("Received command of type: " + command.getCommandType());
        
        // Handle the command based on its type
        switch (command.getCommandType()) {
            case JOIN_PLAYER -> handleJoinPlayerCommand(session, (JoinPlayer) command);
            case JOIN_OBSERVER -> handleJoinObserverCommand(session, (JoinObserver) command);
            case MAKE_MOVE -> handleMakeMoveCommand(session, (MakeMove) command);
            case LEAVE -> handleLeaveCommand(session, (Leave) command);
            case RESIGN -> handleResignCommand(session, (Resign) command);
            default -> session.getRemote().sendString("Unknown command type");
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
    
    private void sendMessage(Session session, String message) {
        try {
            session.getRemote().sendString(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void addSessionToGame(int gameID, Session session) {
        gameSessions.computeIfAbsent(gameID, key -> new HashSet<>()).add(session);
    }
    
    private void removeSessionFromGame(int gameID, Session session) {
        gameSessions.computeIfPresent(gameID, (key, sessions) -> {
            sessions.remove(session);
            return sessions.isEmpty() ? null : sessions;
        });
    }
    
    private void sendMessageToOtherPlayers(int gameID, Session excludeSession, String message) {
        // Send the message to all clients in the game except the excluded session
        Set<Session> sessions = gameSessions.get(gameID);
        if (sessions != null) {
            sessions.stream()
                    .filter(session -> !session.equals(excludeSession))
                    .forEach(session -> sendMessage(session, message));
        }
    }
    
    private void sendMessageToAllPlayers(int gameID, String message) {
        // Implement the logic to send the message to all clients in the game
        Set<Session> sessions = gameSessions.get(gameID);
        if (sessions != null) {
            sessions.forEach(session -> sendMessage(session, message));
        }
    }
    
    static class UserGameCommandDeserializer implements JsonDeserializer<UserGameCommand> {
        @Override
        public UserGameCommand deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            UserGameCommand.CommandType type = UserGameCommand.CommandType.valueOf(jsonObject.get("commandType").getAsString());
            
            return switch (type) {
                case JOIN_PLAYER -> context.deserialize(jsonObject, JoinPlayer.class);
                case JOIN_OBSERVER -> context.deserialize(jsonObject, JoinObserver.class);
                case MAKE_MOVE -> context.deserialize(jsonObject, MakeMove.class);
                case LEAVE -> context.deserialize(jsonObject, Leave.class);
                case RESIGN -> context.deserialize(jsonObject, Resign.class);
            };
        }
    }
}
    
    
