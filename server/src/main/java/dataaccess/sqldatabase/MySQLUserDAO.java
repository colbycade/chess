package dataaccess.sqldatabase;

import dataaccess.DatabaseManager;
import dataaccess.UserDAO;
import exception.DataAccessException;
import model.UserData;

import java.sql.*;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class MySQLUserDAO implements UserDAO {


    @Override
    public void insertUser(UserData user) throws DataAccessException {

    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    @Override
    public void clear() throws DataAccessException {

    }
}
