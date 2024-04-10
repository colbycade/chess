package ui;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.userCommands.*;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

public class WebSocketCommunicator extends Endpoint {
    
    private final Gson gson = new Gson();
    public Session session;
    private final ServerMessageObserver observer;
    
    public WebSocketCommunicator(Integer port, ServerMessageObserver observer) throws Exception {
        URI uri = new URI("ws://localhost:" + port + "/connect");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);
        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                System.out.println(message);
            }
        });
        this.observer = observer;
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
    
    private void handleReceivedMessage(String message) {
        ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
        observer.notify(serverMessage);
    }
}