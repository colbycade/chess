package ui;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.*;
import webSocketMessages.serverMessages.LoadGame;
import webSocketMessages.serverMessages.Notification;
import webSocketMessages.serverMessages.Error;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.userCommands.*;

import javax.websocket.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;

public class WebSocketCommunicator extends Endpoint {
    
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ServerMessage.class, new ServerMessageDeserializer())
            .create();
    public Session session;
    
    public WebSocketCommunicator(Integer port, ServerMessageObserver observer) throws Exception {
        URI uri = new URI("ws://localhost:" + port + "/connect");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.setDefaultMaxSessionIdleTimeout(20 * 60 * 1000); // 20 minutes
        this.session = container.connectToServer(this, uri);
        System.out.println("Connected to websocket server");
        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
                System.out.println("Received message of type: " + serverMessage.getServerMessageType());
                observer.notify(serverMessage);
            }
        });
    }
    
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
    
    public void send(String msg) throws IOException {
        this.session.getBasicRemote().sendText(msg);
    }
    
    public void sendJoinPlayerCommand(String authToken, int gameID, ChessGame.TeamColor playerColor) throws IOException {
        JoinPlayer command = new JoinPlayer(authToken, gameID, playerColor);
        String jsonCommand = gson.toJson(command);
        send(jsonCommand);
    }
    
    public void sendJoinObserverCommand(String authToken, int gameID) throws IOException {
        JoinObserver command = new JoinObserver(authToken, gameID);
        String jsonCommand = gson.toJson(command);
        send(jsonCommand);
    }
    
    public void sendMakeMoveCommand(String authToken, Integer gameID, ChessMove move) throws IOException {
        MakeMove command = new MakeMove(authToken, gameID, move);
        String jsonCommand = gson.toJson(command);
        send(jsonCommand);
    }
    
    public void sendLeaveCommand(String authToken, Integer gameID) throws IOException {
        Leave command = new Leave(authToken, gameID);
        String jsonCommand = gson.toJson(command);
        send(jsonCommand);
    }
    
    public void sendResignCommand(String authToken, Integer gameID) throws IOException {
        Resign command = new Resign(authToken, gameID);
        String jsonCommand = gson.toJson(command);
        send(jsonCommand);
    }
    
    public static class ServerMessageDeserializer implements JsonDeserializer<ServerMessage> {
        @Override
        public ServerMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            ServerMessage.ServerMessageType type = ServerMessage.ServerMessageType.valueOf(jsonObject.get(
                    "serverMessageType").getAsString());
            
            return switch (type) {
                case NOTIFICATION -> context.deserialize(jsonObject, Notification.class);
                case LOAD_GAME -> context.deserialize(jsonObject, LoadGame.class);
                case ERROR -> context.deserialize(jsonObject, Error.class);
            };
        }
    }
}