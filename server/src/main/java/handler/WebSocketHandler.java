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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {
    
    private final GameService gameService;
    private final AuthDAO authDAO;
    final Gson gson = new GsonBuilder()
            .registerTypeAdapter(UserGameCommand.class, new UserGameCommandDeserializer())
            .registerTypeAdapter(UserGameCommand.class, new UserGameCommandSerializer())
            .create();
    private final Map<Integer, Set<Session>> gameSessions;    // Map of game IDs to the set of sessions in the game
    
    public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameService = new GameService(authDAO, gameDAO);
        gameSessions = new ConcurrentHashMap<>();
    }
    
    @OnWebSocketConnect
    public void onConnect(Session session) {
        // Handle new WebSocket connection
        System.out.println("WebSocket connection opened: " + session.getRemoteAddress());
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
        sendMessage(session, new Error("An unexpected error occurred"));
    }
    
    private void handleJoinPlayerCommand(Session session, JoinPlayer command) throws DataAccessException {
        // Load the game state for root client
        GameData gameData = getGameData(session, command.getAuthString(), command.gameID());
        if (gameData == null) return;
        
        String username = authDAO.getAuth(command.getAuthString()).username();
        String actualUser = command.playerColor() == ChessGame.TeamColor.WHITE ? gameData.whiteUsername() :
                gameData.blackUsername();
        if (actualUser == null) {
            sendMessage(session, new Error("Failed to join game"));
            return;
        }
        if (!actualUser.equals(username)) {
            sendMessage(session, new Error("Attempted to join game as wrong user"));
            return;
        }
        
        LoadGame loadGame = new LoadGame(gameData);
        sendMessage(session, loadGame);
        
        // Add root client's session to the game
        addSessionToGame(command.gameID(), session);
        
        // Notify other players
        Notification notification = new Notification(SET_TEXT_COLOR_GREEN + "Player " + SET_TEXT_COLOR_BLUE + username +
                SET_TEXT_COLOR_GREEN + " joined the game as " + SET_TEXT_COLOR_BLUE + command.playerColor() + RESET_ALL);
        sendMessageToOtherPlayers(command.gameID(), session, notification);
    }
    
    private void handleJoinObserverCommand(Session session, JoinObserver command) throws DataAccessException {
        // Load the game state for root client
        GameData gameData = getGameData(session, command.getAuthString(), command.gameID());
        if (gameData == null) return;
        
        LoadGame loadGame = new LoadGame(gameData);
        sendMessage(session, loadGame);
        
        // Add root client's session to the game
        addSessionToGame(command.gameID(), session);
        
        // Notify other players
        String username = authDAO.getAuth(command.getAuthString()).username();
        Notification notification = new Notification(SET_TEXT_COLOR_BLUE + username +
                SET_TEXT_COLOR_GREEN + " started observing this game!" + RESET_ALL);
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
        GameData gameData = getGameData(session, command.getAuthString(), command.gameID());
        if (gameData == null) return;
        String username = authDAO.getAuth(command.getAuthString()).username();
        LoadGame loadGame = new LoadGame(gameData);
        sendMessageToOtherPlayers(command.gameID(), session, new Notification(SET_TEXT_COLOR_GREEN +
                "Player " + SET_TEXT_COLOR_BLUE + username + SET_TEXT_COLOR_GREEN +
                " made the move: " + SET_TEXT_COLOR_BLUE + move + RESET_ALL));
        sendMessageToAllPlayers(command.gameID(), loadGame);
        
        // Check if the game is over
        if (gameData.game().getWinner() != null) {
            Notification gameOverNotification;
            if (gameData.game().getWinner() == ChessGame.TeamColor.DRAW) {
                gameOverNotification = new Notification(SET_TEXT_COLOR_GREEN + "Game over! It's a draw!" + RESET_ALL);
            } else {
                gameOverNotification = new Notification(SET_TEXT_COLOR_GREEN + "Game over! Winner: " +
                        SET_TEXT_COLOR_BLUE + gameData.game().getWinner() + RESET_ALL);
            }
            sendMessageToAllPlayers(command.gameID(), gameOverNotification);
        }
    }
    
    private void handleLeaveCommand(Session session, Leave command) throws Exception {
        GameData gameData = getGameData(session, command.getAuthString(), command.gameID());
        if (gameData == null) return;
        
        // Remove from database
        try {
            gameService.leaveGame(command);
        } catch (DataAccessException e) {
            sendMessage(session, new Error("Failed to leave game"));
            return;
        }
        
        // Notify other players
        String username = authDAO.getAuth(command.getAuthString()).username();
        Notification notification;
        if (!Objects.equals(gameData.whiteUsername(), username) &&
                !Objects.equals(gameData.blackUsername(), username)) {
            notification = new Notification(SET_TEXT_COLOR_BLUE + username +
                    SET_TEXT_COLOR_GREEN + " stopped observing the game" + RESET_ALL);
        } else {
            notification = new Notification(SET_TEXT_COLOR_BLUE + username +
                    SET_TEXT_COLOR_GREEN + " left the game" + RESET_ALL);
        }
        sendMessageToOtherPlayers(command.gameID(), session, notification);
        
        // Remove the session from the game
        removeSessionFromGame(command.gameID(), session);
    }
    
    private void handleResignCommand(Session session, Resign command) throws Exception {
        GameData gameData = getGameData(session, command.getAuthString(), command.gameID());
        if (gameData == null) return;
        if (gameData.game().getWinner() != null) {
            sendMessage(session, new Error("Game is already over"));
            return;
        }
        String username = authDAO.getAuth(command.getAuthString()).username();
        
        // Mark game as over
        try {
            gameService.resignGame(command);
        } catch (DataAccessException e) {
            sendMessage(session, new Error("Failed to resign game"));
            return;
        }
        
        // Notify other players
        Notification notification = new Notification(SET_TEXT_COLOR_BLUE + username +
                SET_TEXT_COLOR_GREEN + " resigned the game" + RESET_ALL);
        sendMessageToAllPlayers(command.gameID(), notification);
    }
    
    private void sendMessage(Session session, ServerMessage message) {
        try {
            String jsonServerMessage = gson.toJson(message);
            System.out.println("Sending message of type: " + message.getServerMessageType());
            if (message.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME) {
                LoadGame loadGame = (LoadGame) message;
                System.out.println("    with board state: \n" + loadGame.gameData().game().getBoard());
            } else if (message.getServerMessageType() == ServerMessage.ServerMessageType.NOTIFICATION) {
                Notification notification = (Notification) message;
                System.out.println("    with message: " + notification.message());
            } else if (message.getServerMessageType() == ServerMessage.ServerMessageType.ERROR) {
                Error error = (Error) message;
                System.out.println("    with error message: " + error.errorMessage());
            }
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
    
    private GameData getGameData(Session session, String authToken, Integer gameID) throws DataAccessException {
        Collection<GameData> games;
        try {
            games = gameService.listGames(new ListGamesRequest(authToken)).games();
        } catch (UnauthorizedException e) {
            sendMessage(session, new Error("Unauthorized to list games"));
            return null;
        }
        
        GameData gameData = null;
        for (GameData game : games) {
            if (game.gameID().equals(gameID)) {
                gameData = game;
                break;
            }
        }
        if (gameData == null) {
            sendMessage(session, new Error("Game not found"));
            return null;
        }
        return gameData;
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
    
    // ANSI escape codes for colored text in notifications
    private static final String UNICODE_ESCAPE = "\u001b";
    private static final String RESET_ALL = UNICODE_ESCAPE + "[0m";
    private static final String SET_TEXT_COLOR = UNICODE_ESCAPE + "[38;5;";
    private static final String SET_TEXT_COLOR_GREEN = SET_TEXT_COLOR + "46m";
    private static final String SET_TEXT_COLOR_BLUE = SET_TEXT_COLOR + "12m";
}
    
    
