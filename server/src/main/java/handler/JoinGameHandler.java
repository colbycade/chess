package handler;

import spark.Request;
import spark.Response;
import spark.Route;

public class JoinGameHandler implements Route {
    private static JoinGameHandler instance = null;

    // Private constructor to prevent direct instantiation
    private JoinGameHandler() {
    }

    // Public method to get the singleton instance
    public static JoinGameHandler getInstance() {
        if (instance == null) {
            instance = new JoinGameHandler();
        }
        return instance;
    }

    @Override
    public Object handle(Request req, Response res) {
        // Your handler logic here
        return null;
    }
}
