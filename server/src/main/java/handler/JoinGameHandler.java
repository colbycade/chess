package handler;

import com.google.gson.Gson;
import dataAccess.AuthDAO;
import dataAccess.GameDAO;
import exception.DataAccessException;
import service.GameService;
import model.request.JoinGameRequest;
import spark.Request;
import spark.Response;
import spark.Route;
import chess.ChessGame.TeamColor;

import java.util.Map;

public class JoinGameHandler implements Route {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public JoinGameHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.gameService = new GameService(authDAO, gameDAO);
    }

    @Override
    public Object handle(Request req, Response res) throws DataAccessException {
        String authToken = req.headers("authorization");
        var body = gson.fromJson(req.body(), Map.class);
        String playerColorString = (String) body.get("playerColor"); // account for null
        TeamColor playerColor = playerColorString != null ? TeamColor.valueOf(playerColorString) : null;
        Double gameIDDouble = (Double) body.get("gameID"); // gson converts numbers to Double by default
        int gameID = gameIDDouble.intValue();
        JoinGameRequest joinGameRequest = new JoinGameRequest(authToken, playerColor, gameID);
        gameService.joinGame(joinGameRequest);
        res.status(200);
        res.type("application/json");
        return "{}"; // No response body
    }
}
