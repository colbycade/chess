package handler;

import spark.Request;
import spark.Response;
import spark.Route;

public class RegisterHandler implements Route {
    private static RegisterHandler instance = null;

    // Private constructor to prevent instantiation
    private RegisterHandler() {
    }

    // Static method to get instance
    public static RegisterHandler getInstance() {
        if (instance == null) {
            instance = new RegisterHandler();
        }
        return instance;
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        // Your handler logic here
        return null;
    }
}
