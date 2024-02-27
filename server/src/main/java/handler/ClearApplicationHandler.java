package handler;

import dataAccess.AuthDAO;
import dataAccess.GameDAO;
import dataAccess.UserDAO;
import dataAccess.InMemoryDatabase.MemoryAuthDAO;
import dataAccess.InMemoryDatabase.MemoryGameDAO;
import dataAccess.InMemoryDatabase.MemoryUserDAO;
import exception.DataAccessException;
import service.GameService;
import service.UserService;
import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Route;

public class ClearApplicationHandler implements Route {
    private final GameDAO gameDAO = MemoryGameDAO.getInstance();
    private final AuthDAO authDAO = MemoryAuthDAO.getInstance();
    private final UserDAO userDAO = MemoryUserDAO.getInstance();
    private final GameService gameService = new GameService(gameDAO, authDAO);
    private final UserService userService = new UserService(userDAO, authDAO);
    private final Gson gson = new Gson();

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
    public Object handle(Request req, Response res) throws DataAccessException {
        userService.clearService();
        gameService.clearService();
        res.status(200);
        return ""; // No response body
    }
}
