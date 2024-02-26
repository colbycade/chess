package handler;

import dataAccess.AuthDAO;
import dataAccess.InMemoryDatabase.MemoryAuthDAO;
import dataAccess.InMemoryDatabase.MemoryUserDAO;
import dataAccess.UserDAO;
import exception.AlreadyTakenException;
import exception.BadRequestException;
import exception.DataAccessException;
import service.UserService;
import service.request.RegisterRequest;
import service.response.RegisterResponse;
import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class RegisterHandler implements Route {
    private final UserDAO userDAO = MemoryUserDAO.getInstance();
    private final AuthDAO authDAO = MemoryAuthDAO.getInstance();
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
    public Object handle(Request req, Response res) throws DataAccessException {
        RegisterRequest registerRequest = gson.fromJson(req.body(), RegisterRequest.class);
        RegisterResponse registerResponse = userService.register(registerRequest);
        res.status(200);
        res.type("application/json");
        return gson.toJson(registerResponse);
    }

}
