package dataAccess;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;

public class MemoryGameDAO implements GameDAO {
    private HashMap<Integer, GameData> games;
    private int gameIDCounter = 0; // Counter for auto-incrementing game IDs

    public MemoryGameDAO() {
        games = new HashMap<>();
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        int newGameID = ++gameIDCounter;
        // Assuming GameData has a constructor or method to set gameID.
        // If GameData's gameID is final, you might need to create a new instance of GameData with the newGameID here.
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
}
