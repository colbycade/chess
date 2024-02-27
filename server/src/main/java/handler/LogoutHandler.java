package handler;

import com.google.gson.Gson;
import dataAccess.AuthDAO;
import dataAccess.InMemoryDatabase.MemoryAuthDAO;
import dataAccess.InMemoryDatabase.MemoryUserDAO;
import dataAccess.UserDAO;
import exception.UnauthorizedException;
import service.UserService;
import service.request.LogoutRequest;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class LogoutHandler implements Route {
    private final UserDAO userDAO = MemoryUserDAO.getInstance();
    private final AuthDAO authDAO = MemoryAuthDAO.getInstance();
    private final UserService userService = new UserService(userDAO, authDAO);
    private final Gson gson = new Gson();

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
        LogoutRequest logoutRequest = new LogoutRequest(req.headers("authorization"));
        userService.logout(logoutRequest);
        res.status(200);
        res.type("application/json");
        return "{}"; // No response body
    }
}
