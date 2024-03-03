package handler;

import com.google.gson.Gson;
import exception.DataAccessException;
import service.GameService;
import service.request.CreateGameRequest;
import service.response.CreateGameResponse;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class CreateGameHandler implements Route {
    private final GameService gameService = new GameService();
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
        String authToken = req.headers("authorization");
        var body = gson.fromJson(req.body(), Map.class);
        String gameName = (String) body.get("gameName");
        CreateGameRequest createGameRequest = new CreateGameRequest(authToken, gameName);
        CreateGameResponse createGameResponse = gameService.createGame(createGameRequest);
        res.status(200);
        res.type("application/json");
        return gson.toJson(createGameResponse);
    }
}
