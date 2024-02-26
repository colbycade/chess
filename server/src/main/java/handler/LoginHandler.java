package handler;

import com.google.gson.Gson;
import dataAccess.InMemoryDatabase.MemoryAuthDAO;
import dataAccess.InMemoryDatabase.MemoryUserDAO;
import dataAccess.AuthDAO;
import dataAccess.UserDAO;
import dataAccess.exception.UnauthorizedException;
import service.UserService;
import service.request.LoginRequest;
import service.response.LoginResponse;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class LoginHandler implements Route {
    private final UserDAO userDAO = MemoryUserDAO.getInstance();
    private final AuthDAO authDAO = MemoryAuthDAO.getInstance();
    private final UserService userService = new UserService(userDAO, authDAO);
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
    public Object handle(Request req, Response res) {
        try {
            LoginRequest loginRequest = gson.fromJson(req.body(), LoginRequest.class);
            LoginResponse loginResponse = userService.login(loginRequest);
            res.status(200);
            res.type("application/json");
            return gson.toJson(loginResponse);
        } catch (UnauthorizedException e) {
            res.status(401);
            return gson.toJson(Map.of("message", "Error: unauthorized"));
        } catch (Exception e) {
            // Handle any other exceptions
            res.status(500);
            return gson.toJson(Map.of("message", "Error: description"));
        }
    }
}
