package dataAccess;

import model.GameData;
import model.UserData;

import java.util.ArrayList;
import java.util.List;

public class MemoryGameDAO implements GameDAO {
    private ArrayList<GameData> Games;

    public MemoryGameDAO() {
        Games = new ArrayList<>();
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {

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