package ui;

import javax.websocket.*;
import java.net.URI;
import java.util.Scanner;

public class WebSocketCommunicator extends Endpoint {
    
    public static void main(String[] args) throws Exception {
        var ws = new WebSocketCommunicator(8080);
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Enter a message you want to echo");
        while (true) ws.send(scanner.nextLine());
    }
    
    public Session session;
    
    public WebSocketCommunicator(Integer port) throws Exception {
        URI uri = new URI("ws://localhost:" + port + "/connect");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);
        
        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                System.out.println(message);
            }
        });
    }
    
    public void send(String msg) throws Exception {
        this.session.getBasicRemote().sendText(msg);
    }
    
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}