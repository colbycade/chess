package handler;

import dataAccess.AuthDAO;
import dataAccess.MemoryAuthDAO;
import dataAccess.MemoryUserDAO;
import dataAccess.UserDAO;
import model.*;
import service.UserService;
import service.request.RegisterRequest;
import service.response.RegisterResponse;
import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Route;

public class RegisterHandler implements Route {
    private final UserDAO userDAO = new MemoryUserDAO();
    private final AuthDAO authDAO = new MemoryAuthDAO();
    private final UserService userService = new UserService(userDAO, authDAO);
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
    public Object handle(Request req, Response res) {
        try {
            RegisterRequest registerRequest = gson.fromJson(req.body(), RegisterRequest.class);
            RegisterResponse registerResponse = userService.register(registerRequest);
            res.status(200); // HTTP 200 OK
            res.type("application/json");
            return gson.toJson(new RegisterResponse(registerResponse.authToken()));
        } catch (Exception e) {
            res.status(500); // HTTP 500 Internal Server Error
            return e;
        }
    }

}
