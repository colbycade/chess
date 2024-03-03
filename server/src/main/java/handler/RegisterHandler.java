package handler;

import exception.DataAccessException;
import service.UserService;
import service.request.RegisterRequest;
import service.response.RegisterResponse;
import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Route;

public class RegisterHandler implements Route {
    private final UserService userService = new UserService();
    private final Gson gson = new Gson();

    private static RegisterHandler instance = null;

    // Private constructor to prevent direct instantiation
    private RegisterHandler() {
    }

    // Public method to get the singleton instance
    public static RegisterHandler getInstance() {
        if (instance == null) {
            instance = new RegisterHandler();
        }
        return instance;
    }

    @Override
    public Object handle(Request req, Response res) throws DataAccessException {
        RegisterRequest registerRequest = gson.fromJson(req.body(), RegisterRequest.class);
        RegisterResponse registerResponse = userService.register(registerRequest);
        res.status(200);
        res.type("application/json");
        return gson.toJson(registerResponse);
    }

}
