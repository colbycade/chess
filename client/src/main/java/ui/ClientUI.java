package ui;

public class ClientUI {
    private ServerFacade serverFacade;

    public ClientUI(Integer port) {
        serverFacade = new ServerFacade(port);
    }

    public void start() {
    }
}