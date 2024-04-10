import ui.ChessClient;

public class Main {
    public static void main(String[] args) {
        ChessClient chessClient = new ChessClient(8080);
        chessClient.start();
    }
}