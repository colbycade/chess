package dataaccess.sqldatabase;

import dataaccess.GameDAO;
import exception.DataAccessException;
import model.GameData;

import java.util.Collection;

public class MySQLGameDAO implements GameDAO {

    @Override
    public int createGame(String gameName) throws DataAccessException {
        return 0;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {

    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return null;
    }

    @Override
    public void clear() throws DataAccessException {

    }
}
