package handler;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import dataaccess.inmemorydatabase.MemoryAuthDAO;
import dataaccess.inmemorydatabase.MemoryGameDAO;
import dataaccess.inmemorydatabase.MemoryUserDAO;
import exception.DataAccessException;
import service.GameService;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

public class ClearApplicationHandler implements Route {
    private final GameDAO gameDAO = MemoryGameDAO.getInstance();
    private final AuthDAO authDAO = MemoryAuthDAO.getInstance();
    private final UserDAO userDAO = MemoryUserDAO.getInstance();
    private final GameService gameService = new GameService(gameDAO, authDAO);
    private final UserService userService = new UserService(userDAO, authDAO);

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
        userService.clearService();
        gameService.clearService();
        res.type("application/json");
        return "{}"; // No response body
    }
}
