package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import exception.AlreadyTakenException;
import exception.BadRequestException;
import exception.DataAccessException;
import model.GameData;
import model.AuthData;
import service.request.CreateGameRequest;
import service.request.JoinGameRequest;
import service.request.ListGamesRequest;
import service.response.CreateGameResponse;
import service.response.ListGamesResponse;

import static service.AuthUtil.verifyAuthToken;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public CreateGameResponse createGame(CreateGameRequest request) throws DataAccessException {
        verifyAuthToken(authDAO, request.authToken());

        // Verify that the game name is not null
        if (request.gameName() == null) {
            throw new BadRequestException("bad request");
        }

        // Create game
        int gameId = gameDAO.createGame(request.authToken());
        return new CreateGameResponse(gameId);
    }

    public ListGamesResponse listGames(ListGamesRequest request) throws DataAccessException {
        verifyAuthToken(authDAO, request.authToken());

        // Get games
        return new ListGamesResponse(gameDAO.listGames());
    }

    public void joinGame(JoinGameRequest request) throws DataAccessException {
        verifyAuthToken(authDAO, request.authToken());

        // Get client's username
        AuthData auth = authDAO.getAuth(request.authToken());
        String username = auth.username();

        // Verify that the game exists
        GameData game = gameDAO.getGame(request.gameID());
        if (game == null) {
            throw new BadRequestException("bad request");
        }

        // Update game data according to client's request
        GameData updatedGame = getGameData(request, game, username);
        gameDAO.updateGame(updatedGame);
    }

    private static GameData getGameData(JoinGameRequest request, GameData game, String username) throws AlreadyTakenException {
        // If client supplies a color, check if available and update
        if ((request.clientColor() == ChessGame.TeamColor.WHITE && game.whiteUsername() != null) ||
                (request.clientColor() == ChessGame.TeamColor.BLACK && game.blackUsername() != null)) {
            throw new AlreadyTakenException("already taken");
        }
        String newWhiteUsername = request.clientColor() == ChessGame.TeamColor.WHITE ? username : game.whiteUsername();
        String newBlackUsername = request.clientColor() == ChessGame.TeamColor.BLACK ? username : game.blackUsername();

        // TODO: Else, client will join as an observer
//        if (request.clientColor() == null) {
//        }

        // Return updated game data
        return new GameData(request.gameID(), newWhiteUsername, newBlackUsername, game.gameName(), game.game());
    }

    public void clearService() throws DataAccessException {
        gameDAO.clear();
    }
}