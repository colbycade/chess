package handler;

import com.google.gson.Gson;
import exception.DataAccessException;
import service.UserService;
import service.request.LoginRequest;
import service.response.LoginResponse;
import spark.Request;
import spark.Response;
import spark.Route;

public class LoginHandler implements Route {
    private final UserService userService = new UserService();
    private final Gson gson = new Gson();

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
    public Object handle(Request req, Response res) throws DataAccessException {
        LoginRequest loginRequest = gson.fromJson(req.body(), LoginRequest.class);
        LoginResponse loginResponse = userService.login(loginRequest);
        res.status(200);
        res.type("application/json");
        return gson.toJson(loginResponse);
    }
}
