package service;

import chess.ChessGame;
import dataAccess.AuthDAO;
import dataAccess.GameDAO;
import exception.AlreadyTakenException;
import exception.BadRequestException;
import exception.DataAccessException;
import model.GameData;
import model.AuthData;
import model.request.CreateGameRequest;
import model.request.JoinGameRequest;
import model.request.ListGamesRequest;
import model.response.CreateGameResponse;
import model.response.ListGamesResponse;

import static service.AuthUtil.verifyAuthToken;

public class GameService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public GameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public CreateGameResponse createGame(CreateGameRequest request) throws DataAccessException {
        verifyAuthToken(authDAO, request.authToken());

        // Verify that the game name is not null
        if (request.gameName() == null) {
            throw new BadRequestException("game name cannot be null");
        }

        // Create game
        int gameID = gameDAO.createGame(request.gameName());
        return new CreateGameResponse(gameID);
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
            throw new BadRequestException("game does not exist");
        }

        // Update game data according to client's request
        GameData updatedGame = getGameData(request, game, username);
        gameDAO.updateGame(updatedGame);
    }

    private static GameData getGameData(JoinGameRequest request, GameData game, String username) throws AlreadyTakenException {
        // If client supplies a color, check if available and update
        if ((request.clientColor() == ChessGame.TeamColor.WHITE && game.whiteUsername() != null) ||
                (request.clientColor() == ChessGame.TeamColor.BLACK && game.blackUsername() != null)) {
            throw new AlreadyTakenException("color already taken");
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