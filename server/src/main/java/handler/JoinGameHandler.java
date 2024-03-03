package handler;

import com.google.gson.Gson;
import exception.DataAccessException;
import service.GameService;
import service.request.JoinGameRequest;
import spark.Request;
import spark.Response;
import spark.Route;
import chess.ChessGame.TeamColor;

import java.util.Map;

public class JoinGameHandler implements Route {
    private final GameService gameService = new GameService();
    private final Gson gson = new Gson();

    private static JoinGameHandler instance = null;
    // Private constructor to prevent direct instantiation

    private JoinGameHandler() {
    }

    // Public method to get the singleton instance
    public static JoinGameHandler getInstance() {
        if (instance == null) {
            instance = new JoinGameHandler();
        }
        return instance;
    }

    @Override
    public Object handle(Request req, Response res) throws DataAccessException {
        String authToken = req.headers("authorization");
        var body = gson.fromJson(req.body(), Map.class);
        String clientColorString = (String) body.get("playerColor"); // account for null
        TeamColor clientColor = clientColorString != null ? TeamColor.valueOf(clientColorString) : null;
        Double gameIDDouble = (Double) body.get("gameID"); // gson converts numbers to Double by default
        int gameID = gameIDDouble.intValue();
        JoinGameRequest joinGameRequest = new JoinGameRequest(authToken, clientColor, gameID);
        gameService.joinGame(joinGameRequest);
        res.status(200);
        res.type("application/json");
        return "{}"; // No response body
    }
}
