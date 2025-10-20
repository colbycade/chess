import ui.ChessClient;

public class Main {
    public static void main(String[] args) {
        // Default host and port
        String host = "localhost";
        int port = 8080;
        
        // Parse "hostname:port" argument (e.g. chess.family-tasks.app:8080)
        if (args.length > 0 && args[0].contains(":")) {
            String[] parts = args[0].split(":");
            host = parts[0];
            try {
                port = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default 8080.");
            }
        }
        
        try {
            new ChessClient(host, port).start();
        } catch (Exception e) {
            System.err.println("Failed to start Chess Client: " + e.getMessage());
            System.exit(1);
        }
    }
}