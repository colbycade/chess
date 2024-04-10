package handler;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.*;
import dataAccess.AuthDAO;
import dataAccess.GameDAO;
import exception.BadRequestException;
import exception.DataAccessException;
import exception.UnauthorizedException;
import model.GameData;
import model.request.ListGamesRequest;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import service.GameService;
import webSocketMessages.serverMessages.Error;
import webSocketMessages.serverMessages.LoadGame;
import webSocketMessages.serverMessages.Notification;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.userCommands.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {
    
    private final GameService gameService;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(UserGameCommand.class, new UserGameCommandDeserializer())
            .registerTypeAdapter(UserGameCommand.class, new UserGameCommandSerializer())
            .create();
    private Map<Integer, Set<Session>> gameSessions;    // Map of game IDs to the set of sessions in the game
    
    public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
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
    
    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        // Handle WebSocket error
        System.out.println("WebSocket error: " + error.getMessage());
        sendMessage(session, new Error("Error: " + error.getMessage()));
    }
    
    private void handleJoinPlayerCommand(Session session, JoinPlayer command) throws DataAccessException {
        // Load the game state for root client
        Collection<GameData> games;
        try {
            games = gameService.listGames(new ListGamesRequest(command.getAuthString())).games();
        } catch (UnauthorizedException e) {
            sendMessage(session, new Error("Unauthorized to list games"));
            return;
        }
        
        GameData gameData = null;
        for (GameData game : games) {
            if (game.gameID().equals(command.gameID())) {
                gameData = game;
                break;
            }
        }
        
        String username = command.playerColor() == ChessGame.TeamColor.WHITE ? gameData.whiteUsername() : gameData.blackUsername();
        if (username == null) {
            sendMessage(session, new Error("Game not found"));
            return;
        }
        
        LoadGame loadGame = new LoadGame(gameData);
        sendMessage(session, loadGame);
        
        // Add root client's session to the game
        addSessionToGame(command.gameID(), session);
        
        // Notify other players
        Notification notification = new Notification("Player \u001b[38;5;12m" + username +
                "\u001b[38;5;46m joined the game as \u001b[38;5;12m" + command.playerColor());
        sendMessageToOtherPlayers(command.gameID(), session, notification);
    }
    
    private void handleJoinObserverCommand(Session session, JoinObserver command) throws DataAccessException {
        // Load the game state for root client
        Collection<GameData> games;
        try {
            games = gameService.listGames(new ListGamesRequest(command.getAuthString())).games();
        } catch (UnauthorizedException e) {
            sendMessage(session, new Error("Unauthorized to list games"));
            return;
        }
        
        GameData gameData = null;
        for (GameData game : games) {
            if (game.gameID().equals(command.gameID())) {
                gameData = game;
                break;
            }
        }
        
        LoadGame loadGame = new LoadGame(gameData);
        sendMessage(session, loadGame);
        
        // Add root client's session to the game
        addSessionToGame(command.gameID(), session);
        
        // Notify other players
        Notification notification = new Notification("Someone started observing this game!");
        sendMessageToOtherPlayers(command.gameID(), session, notification);
    }
    
    private void handleMakeMoveCommand(Session session, MakeMove command) throws Exception {
        try {
            gameService.makeMove(command);
        } catch (BadRequestException e) {
            sendMessage(session, new Error(e.getMessage()));
            return;
        }
        ChessMove move = command.move();
        
        // Notify and load the updated game state for all players
        GameData gameData = gameDAO.getGame(command.gameID());
        String username = gameData.game().getTeamTurn() == ChessGame.TeamColor.WHITE ? gameData.blackUsername() :
                gameData.whiteUsername();
        LoadGame loadGame = new LoadGame(gameData);
        sendMessageToOtherPlayers(command.gameID(), session, new Notification("Player " +
                "\u001b[38;5;12m" + username + "\u001b[38;5;46m made the move: \u001b[38;5;12m" + move));
        sendMessageToAllPlayers(command.gameID(), loadGame);
        
        // Check if the game is over
        if (gameData.game().getWinner() != null) {
            Notification gameOverNotification = null;
            if (gameData.game().getWinner() == ChessGame.TeamColor.DRAW) {
                gameOverNotification = new Notification("Game over! It's a draw!");
            } else {
                gameOverNotification = new Notification("Game over! Winner: \u001b[38;5;12m" + gameData.game().getWinner());
            }
            sendMessageToAllPlayers(command.gameID(), gameOverNotification);
        }
    }
    
    private void handleLeaveCommand(Session session, Leave command) throws Exception {
        GameData gameData = gameDAO.getGame(command.gameID());
        String username = gameData.game().getWinner() == ChessGame.TeamColor.WHITE ? gameData.blackUsername() :
                gameData.whiteUsername();
        
        // Remove from database
        try {
            gameService.leaveGame(command);
        } catch (DataAccessException e) {
            sendMessage(session, new Error("Failed to leave game"));
            return;
        }
        
        // Remove the session from the game
        removeSessionFromGame(command.gameID(), session);
        
        // Notify other players
        Notification notification = new Notification("\u001b[38;5;12m" + username + "\u001b[38;5;46m left the game");
        sendMessageToOtherPlayers(command.gameID(), session, notification);
    }
    
    private void handleResignCommand(Session session, Resign command) throws Exception {
        GameData gameData = gameDAO.getGame(command.gameID());
        String username = gameData.game().getTeamTurn() == ChessGame.TeamColor.WHITE ? gameData.blackUsername() :
                gameData.whiteUsername();
        
        // Mark game as over
        try {
            gameService.resignGame(command);
        } catch (DataAccessException e) {
            sendMessage(session, new Error("Failed to resign game"));
            return;
        }
        
        // Notify other players
        Notification notification = new Notification("\u001b[38;5;12m" + username + "\u001b[38;5;46m resigned the game");
        sendMessageToAllPlayers(command.gameID(), notification);
    }
    
    private void sendMessage(Session session, ServerMessage message) {
        try {
            String jsonServerMessage = gson.toJson(message);
            System.out.println("Sending message of type: " + message.getServerMessageType() + " with content: " + jsonServerMessage);
            session.getRemote().sendString(jsonServerMessage);
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
    
    private void sendMessageToOtherPlayers(int gameID, Session excludeSession, ServerMessage message) {
        // Send the message to all clients in the game except the excluded session
        Set<Session> sessions = gameSessions.get(gameID);
        if (sessions != null) {
            sessions.stream()
                    .filter(session -> !session.equals(excludeSession))
                    .forEach(session -> sendMessage(session, message));
        }
    }
    
    private void sendMessageToAllPlayers(int gameID, ServerMessage message) {
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
    
    static class UserGameCommandSerializer implements JsonSerializer<UserGameCommand> {
        @Override
        public JsonElement serialize(UserGameCommand src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("commandType", src.getCommandType().toString());
            jsonObject.addProperty("authToken", src.getAuthString());
            
            switch (src.getCommandType()) {
                case JOIN_PLAYER -> {
                    JoinPlayer joinPlayer = (JoinPlayer) src;
                    jsonObject.addProperty("gameID", joinPlayer.gameID());
                    jsonObject.addProperty("playerColor", joinPlayer.playerColor().toString());
                }
                case JOIN_OBSERVER -> {
                    JoinObserver joinObserver = (JoinObserver) src;
                    jsonObject.addProperty("gameID", joinObserver.gameID());
                }
                case MAKE_MOVE -> {
                    MakeMove makeMove = (MakeMove) src;
                    jsonObject.addProperty("gameID", makeMove.gameID());
                    jsonObject.add("move", context.serialize(makeMove.move()));
                }
                case LEAVE -> {
                    Leave leave = (Leave) src;
                    jsonObject.addProperty("gameID", leave.gameID());
                }
                case RESIGN -> {
                    Resign resign = (Resign) src;
                    jsonObject.addProperty("gameID", resign.gameID());
                }
            }
            return jsonObject;
        }
    }
    
}
    
    
