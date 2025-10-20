package dataaccess.inmemorydb;

import chess.ChessGame;
import dataaccess.GameDAO;
import exception.BadRequestException;
import exception.DataAccessException;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;

public class MemoryGameDAO implements GameDAO {
    private final HashMap<Integer, GameData> games = new HashMap<>();
    private int gameIDCounter = 0; // Counter for auto-incrementing game IDs
    
    private static MemoryGameDAO instance = null;
    
    // Private constructor to prevent direct instantiation
    private MemoryGameDAO() {
    }
    
    // Public method to get the singleton instance
    public static MemoryGameDAO getInstance() {
        if (instance == null) {
            instance = new MemoryGameDAO();
        }
        return instance;
    }
    
    @Override
    public Integer createGame(String gameName) throws BadRequestException {
        if (gameName == null) {
            throw new BadRequestException("game name cannot be null.");
        }
        int newGameID = gameIDCounter++;
        ChessGame newGame = new ChessGame();
        GameData newGameData = new GameData(newGameID, null, null, gameName, newGame);
        games.put(newGameID, newGameData);
        return newGameID;
    }
    
    @Override
    public GameData getGame(Integer gameID) throws BadRequestException {
        if (gameID == null) {
            throw new BadRequestException("gameID cannot be null.");
        }
        return games.get(gameID);
    }
    
    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (game == null) {
            throw new BadRequestException("game data cannot be null.");
        }
        if (games.containsKey(game.gameID())) {
            games.put(game.gameID(), game); // overwrite the old game data with the new game
        } else {
            throw new BadRequestException("Game with ID " + game.gameID() + " does not exist.");
        }
    }
    
    @Override
    public Collection<GameData> listGames() {
        return games.values();
    }
    
    @Override
    public void clear() {
        games.clear();
    }
}
