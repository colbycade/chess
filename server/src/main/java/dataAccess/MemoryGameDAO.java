package dataAccess;

import model.GameData;
import model.UserData;

import java.util.HashMap;
import java.util.List;

public class MemoryGameDAO implements GameDAO {
    private HashMap<Integer, GameData> games;

    public MemoryGameDAO() {
        games = new HashMap<>();
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        if (games.containsKey(game.gameID())) {
            throw new DataAccessException("Game already exists.");
        }
        games.put(game.gameID(), game);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {

    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        return null;
    }
}
