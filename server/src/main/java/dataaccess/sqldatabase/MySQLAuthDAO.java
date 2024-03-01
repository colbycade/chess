package dataaccess.sqldatabase;

import dataaccess.AuthDAO;
import exception.DataAccessException;
import model.AuthData;

public class MySQLAuthDAO implements AuthDAO {

    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        return null;
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {

    }

    @Override
    public void clear() throws DataAccessException {

    }
}
