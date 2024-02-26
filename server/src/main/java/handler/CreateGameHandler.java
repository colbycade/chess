package handler;

import com.google.gson.Gson;
import dataAccess.AuthDAO;
import dataAccess.GameDAO;
import dataAccess.InMemoryDatabase.MemoryAuthDAO;
import dataAccess.InMemoryDatabase.MemoryGameDAO;
import exception.DataAccessException;
import service.GameService;
import service.request.CreateGameRequest;
import service.response.CreateGameResponse;
import spark.Request;
import spark.Response;
import spark.Route;

public class CreateGameHandler implements Route {
    private final GameDAO gameDAO = MemoryGameDAO.getInstance();
    private final AuthDAO authDAO = MemoryAuthDAO.getInstance();
    private final GameService gameService = new GameService(gameDAO, authDAO);
    private final Gson gson = new Gson();

    private static CreateGameHandler instance = null;

    // Private constructor to prevent direct instantiation
    private CreateGameHandler() {
    }

    // Public method to get the singleton instance
    public static CreateGameHandler getInstance() {
        if (instance == null) {
            instance = new CreateGameHandler();
        }
        return instance;
    }

    @Override
    public Object handle(Request req, Response res) throws DataAccessException {
        CreateGameRequest createGameRequest = gson.fromJson(req.body(), CreateGameRequest.class);
        CreateGameResponse createGameResponse = gameService.createGame(createGameRequest);
        res.status(200);
        res.type("application/json");
        return gson.toJson(createGameResponse.gameId());
    }
}
