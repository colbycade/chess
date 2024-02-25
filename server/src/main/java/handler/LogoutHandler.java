package handler;

import spark.Request;
import spark.Response;
import spark.Route;

public class LogoutHandler implements Route {
    private static LogoutHandler instance = null;

    // Private constructor to prevent direct instantiation
    private LogoutHandler() {
    }

    // Public method to get the singleton instance
    public static LogoutHandler getInstance() {
        if (instance == null) {
            instance = new LogoutHandler();
        }
        return instance;
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        // Handler logic here
        return null;
    }
}
