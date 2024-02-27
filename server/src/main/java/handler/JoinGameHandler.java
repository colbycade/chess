package handler;

import com.google.gson.Gson;
import dataAccess.AuthDAO;
import dataAccess.GameDAO;
import dataAccess.InMemoryDatabase.MemoryAuthDAO;
import dataAccess.InMemoryDatabase.MemoryGameDAO;
import exception.DataAccessException;
import service.GameService;
import service.request.JoinGameRequest;
import spark.Request;
import spark.Response;
import spark.Route;
import chess.ChessGame.TeamColor;

import java.util.Map;

public class JoinGameHandler implements Route {
    private final GameDAO gameDAO = MemoryGameDAO.getInstance();
    private final AuthDAO authDAO = MemoryAuthDAO.getInstance();
    private final GameService gameService = new GameService(gameDAO, authDAO);
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
        TeamColor clientColor = TeamColor.valueOf((String) body.get("playerColor"));
        Integer gameID = (Integer) body.get("gameID");
        JoinGameRequest joinGameRequest = new JoinGameRequest(authToken, clientColor, gameID);
        gameService.joinGame(joinGameRequest);
        res.status(200);
        return ""; // No response body
    }
}
