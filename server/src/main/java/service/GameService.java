package service;

import dataAccess.AuthDAO;
import dataAccess.GameDAO;
import exception.BadRequestException;
import exception.DataAccessException;
import exception.UnauthorizedException;
import service.request.CreateGameRequest;
import service.request.JoinGameRequest;
import service.request.ListGamesRequest;
import service.response.CreateGameResponse;
import service.response.ListGamesResponse;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public CreateGameResponse createGame(CreateGameRequest request) throws DataAccessException {
        // Verify that the auth token exists
        if (request.authToken() == null || authDAO.getAuth(request.authToken()) == null) {
            throw new UnauthorizedException("unauthorized");
        }

        // Verify that the game name is not null
        if (request.gameName() == null) {
            throw new BadRequestException("bad request");
        }

        // Create game
        int gameId = gameDAO.createGame(request.authToken());
        return new CreateGameResponse(gameId);
    }

    public ListGamesResponse listGames(ListGamesRequest request) throws DataAccessException {
        return null;
    }

    public void joinGame(JoinGameRequest request) throws DataAccessException {

    }
}
