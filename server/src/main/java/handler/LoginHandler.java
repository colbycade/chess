package handler;

import spark.Request;
import spark.Response;
import spark.Route;

public class LoginHandler implements Route {
    private static LoginHandler instance = null;

    // Private constructor to prevent direct instantiation
    private LoginHandler() {
    }

    // Public method to get the singleton instance
    public static LoginHandler getInstance() {
        if (instance == null) {
            instance = new LoginHandler();
        }
        return instance;
    }

    @Override
    public Object handle(Request req, Response res) {
        // Your handler logic here
        return null;
    }
}
