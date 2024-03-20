import ui.ClientUI;

public class Main {
    public static void main(String[] args) {
        ClientUI clientUI = new ClientUI(8080);
        clientUI.start();
    }
}