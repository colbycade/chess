package handler;

import com.google.gson.Gson;
import dataAccess.AuthDAO;
import dataAccess.InMemoryDatabase.MemoryAuthDAO;
import dataAccess.InMemoryDatabase.MemoryUserDAO;
import dataAccess.UserDAO;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

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
        // Your handler logic here
        return null;
    }
}
