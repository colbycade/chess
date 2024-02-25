package handler;

import spark.Request;
import spark.Response;
import spark.Route;

public class CreateGameHandler implements Route {
    private static CreateGameHandler instance = null;

    // Private constructor to prevent direct instantiation
    private CreateGameHandler() {
    }

    // Public method to get the singleton instance
    public static CreateGameHandler getInstance() {
        if (instance == null) {
            instance = new CreateGameHandler();
        }
        return instance;
    }

    @Override
    public Object handle(Request req, Response res) {
        // Your handler logic here
        return null;
    }
}
