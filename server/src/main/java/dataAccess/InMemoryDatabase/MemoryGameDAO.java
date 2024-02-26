package dataAccess.InMemoryDatabase;

import chess.ChessGame;
import exception.DataAccessException;
import dataAccess.GameDAO;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;

public class MemoryGameDAO implements GameDAO {
    private HashMap<Integer, GameData> games = new HashMap<>();
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
    public int createGame(String gameName) throws DataAccessException {
        int newGameID = ++gameIDCounter;
        ChessGame newGame = new ChessGame();
        GameData newGameData = new GameData(newGameID, "", "", gameName, newGame);
        games.put(newGameID, newGameData);
        return newGameID;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return games.get(gameID);
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (games.containsKey(game.gameID())) {
            games.put(game.gameID(), game);
        } else {
            throw new DataAccessException("Game with ID " + game.gameID() + " does not exist.");
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return games.values();
    }

    @Override
    public void clear() throws DataAccessException {
        games.clear();
    }
}
