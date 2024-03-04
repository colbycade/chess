package handler;

import dataAccess.AuthDAO;
import dataAccess.GameDAO;
import dataAccess.UserDAO;
import exception.DataAccessException;
import service.GameService;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

public class ClearApplicationHandler implements Route {
    private final GameService gameService;
    private final UserService userService;

    public ClearApplicationHandler(GameDAO gameDAO, AuthDAO authDAO, UserDAO userDAO) {
        this.gameService = new GameService(authDAO, gameDAO);
        this.userService = new UserService(authDAO, userDAO);
    }

    @Override
    public Object handle(Request req, Response res) throws DataAccessException {
        userService.clearService();
        gameService.clearService();
        res.type("application/json");
        return "{}"; // No response body
    }
}
