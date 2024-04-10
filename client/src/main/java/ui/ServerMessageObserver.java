package ui;

import webSocketMessages.serverMessages.ServerMessage;

public interface ServerMessageObserver {
    public void notify(ServerMessage message);
}
