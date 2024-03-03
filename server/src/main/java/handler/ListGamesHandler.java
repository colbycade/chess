package handler;

import com.google.gson.Gson;
import exception.DataAccessException;
import service.GameService;
import service.request.ListGamesRequest;
import service.response.ListGamesResponse;
import spark.Request;
import spark.Response;
import spark.Route;

public class ListGamesHandler implements Route {
    private final GameService gameService = new GameService();
    private final Gson gson = new Gson();

    private static ListGamesHandler instance = null;

    // Private constructor to prevent direct instantiation
    private ListGamesHandler() {
    }

    // Public method to get the singleton instance
    public static ListGamesHandler getInstance() {
        if (instance == null) {
            instance = new ListGamesHandler();
        }
        return instance;
    }

    @Override
    public Object handle(Request req, Response res) throws DataAccessException {
        String authToken = req.headers("authorization");
        ListGamesRequest listGamesRequest = new ListGamesRequest(authToken);
        ListGamesResponse listGamesResponse = gameService.listGames(listGamesRequest);
        res.status(200);
        res.type("application/json");
        return gson.toJson(listGamesResponse);
    }
}
