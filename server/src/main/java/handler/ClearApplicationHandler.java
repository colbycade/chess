package handler;

import spark.Request;
import spark.Response;
import spark.Route;

public class ClearApplicationHandler implements Route {
    private static ClearApplicationHandler instance = null;

    // Private constructor to prevent direct instantiation
    private ClearApplicationHandler() {
    }

    // Public method to get the singleton instance
    public static ClearApplicationHandler getInstance() {
        if (instance == null) {
            instance = new ClearApplicationHandler();
        }
        return instance;
    }

    @Override
    public Object handle(Request req, Response res) {
        // Your handler logic here
        return null;
    }
}
