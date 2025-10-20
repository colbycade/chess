package handler;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import exception.DataAccessException;
import service.GameService;
import model.request.CreateGameRequest;
import model.response.CreateGameResponse;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class CreateGameHandler implements Route {
    private final GameService gameService;
    private final Gson gson = new Gson();
    
    public CreateGameHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.gameService = new GameService(authDAO, gameDAO);
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
