package service;

import chess.ChessGame;
import chess.InvalidMoveException;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import exception.AlreadyTakenException;
import exception.BadRequestException;
import exception.DataAccessException;
import model.AuthData;
import model.GameData;
import model.request.CreateGameRequest;
import model.request.JoinGameRequest;
import model.request.ListGamesRequest;
import model.response.CreateGameResponse;
import model.response.ListGamesResponse;
import websocket.commands.Leave;
import websocket.commands.MakeMove;
import websocket.commands.Resign;

import static service.AuthUtil.verifyAuthToken;

public class GameService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    
    public GameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }
    
    public synchronized CreateGameResponse createGame(CreateGameRequest request) throws DataAccessException {
        verifyAuthToken(authDAO, request.authToken());
        
        // Verify that the game name is not null
        if (request.gameName() == null) {
            throw new BadRequestException("Game name cannot be null");
        }
        
        // Create game
        int gameID = gameDAO.createGame(request.gameName());
        return new CreateGameResponse(gameID);
    }
    
    public synchronized ListGamesResponse listGames(ListGamesRequest request) throws DataAccessException {
        verifyAuthToken(authDAO, request.authToken());
        
        // Get games
        return new ListGamesResponse(gameDAO.listGames());
    }
    
    public synchronized void joinGame(JoinGameRequest request) throws DataAccessException {
        verifyAuthToken(authDAO, request.authToken());
        
        // Get client's username
        AuthData auth = authDAO.getAuth(request.authToken());
        String username = auth.username();
        
        // Verify that the game exists
        GameData game = gameDAO.getGame(request.gameID());
        if (game == null) {
            throw new BadRequestException("Game does not exist");
        }
        
        // Verify that the color is available
        if ((request.playerColor() == ChessGame.TeamColor.WHITE && game.whiteUsername() != null) ||
                (request.playerColor() == ChessGame.TeamColor.BLACK && game.blackUsername() != null)) {
            throw new AlreadyTakenException("Color already taken");
        }
        
        // Update game data according to client's request
        GameData updatedGame = getGameData(request, game, username);
        gameDAO.updateGame(updatedGame);
    }
    
    // Gameplay commands
    
    public synchronized void makeMove(MakeMove request) throws DataAccessException {
        verifyAuthToken(authDAO, request.getAuthString());
        
        // Get client's username
        AuthData auth = authDAO.getAuth(request.getAuthString());
        String username = auth.username();
        
        // Verify that the game exists
        GameData game = gameDAO.getGame(request.gameID());
        if (game == null) {
            throw new BadRequestException("Game does not exist");
        }
        
        // Verify that the client is a player in the game
        if (!username.equals(game.whiteUsername()) && !username.equals(game.blackUsername())) {
            throw new BadRequestException("Client is not a player in the game");
        }
        
        // Verify that the game is not over
        if (game.game().getWinner() != null) {
            throw new BadRequestException("Game is over");
        }
        
        // Verify that it is the client's turn
        ChessGame.TeamColor currentColor = game.game().getTeamTurn();
        if (currentColor == ChessGame.TeamColor.WHITE && !username.equals(game.whiteUsername()) ||
                (currentColor == ChessGame.TeamColor.BLACK && !username.equals(game.blackUsername()))) {
            throw new BadRequestException("Move out of turn");
        }
        
        // Make move
        try {
            game.game().makeMove(request.move());
        } catch (InvalidMoveException e) {
            throw new BadRequestException("Invalid move");
        }
        gameDAO.updateGame(game);
    }
    
    public synchronized void leaveGame(Leave request) throws DataAccessException {
        verifyAuthToken(authDAO, request.getAuthString());
        
        // Get client's username
        AuthData auth = authDAO.getAuth(request.getAuthString());
        String username = auth.username();
        
        // Verify that the game exists
        GameData game = gameDAO.getGame(request.gameID());
        if (game == null) {
            throw new BadRequestException("Game does not exist");
        }
        
        // If client is not a player in the game, they are an observer so gameData should not be updated
        if (!username.equals(game.whiteUsername()) && !username.equals(game.blackUsername())) {
            return;
        }
        
        // Remove client from game
        if (username.equals(game.whiteUsername())) {
            gameDAO.updateGame(new GameData(game.gameID(), null, game.blackUsername(), game.gameName(), game.game()));
        } else {
            gameDAO.updateGame(new GameData(game.gameID(), game.whiteUsername(), null, game.gameName(), game.game()));
        }
    }
    
    public synchronized void resignGame(Resign request) throws DataAccessException {
        verifyAuthToken(authDAO, request.getAuthString());
        
        // Get client's username
        AuthData auth = authDAO.getAuth(request.getAuthString());
        String username = auth.username();
        
        // Verify that the game exists
        GameData game = gameDAO.getGame(request.gameID());
        if (game == null) {
            throw new BadRequestException("Game does not exist");
        }
        
        // Verify that the client is a player in the game
        if (!username.equals(game.whiteUsername()) && !username.equals(game.blackUsername())) {
            throw new BadRequestException("Client is not a player in the game");
        }
        
        // Resign game
        if (username.equals(game.whiteUsername())) {
            game.game().setWinner(ChessGame.TeamColor.BLACK);
        } else {
            game.game().setWinner(ChessGame.TeamColor.WHITE);
        }
        gameDAO.updateGame(game);
        
    }
    
    
    // Helper methods
    
    private static GameData getGameData(JoinGameRequest request, GameData game, String username) {
        // If client supplies a color, check if available and update
        String newWhiteUsername = request.playerColor() == ChessGame.TeamColor.WHITE ? username : game.whiteUsername();
        String newBlackUsername = request.playerColor() == ChessGame.TeamColor.BLACK ? username : game.blackUsername();
        
        // Else, client will join as an observer, not affecting game data
        
        // Return updated game data
        return new GameData(request.gameID(), newWhiteUsername, newBlackUsername, game.gameName(), game.game());
    }
    
    public void clearService() throws DataAccessException {
        gameDAO.clear();
    }
}