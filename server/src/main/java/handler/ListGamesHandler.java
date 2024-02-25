package handler;

import spark.Request;
import spark.Response;
import spark.Route;

public class ListGamesHandler implements Route {
    private static ListGamesHandler instance = null;

    // Private constructor to prevent direct instantiation
    private ListGamesHandler() {
    }

    // Public method to get the singleton instance
    public static ListGamesHandler getInstance() {
        if (instance == null) {
            instance = new ListGamesHandler();
        }
        return instance;
    }

    @Override
    public Object handle(Request req, Response res) {
        // Your handler logic here
        return null;
    }
}
