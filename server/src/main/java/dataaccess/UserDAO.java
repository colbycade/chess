package dataaccess;

import exception.DataAccessException;
import model.*;

public interface UserDAO {
    // Method to insert a new user into the database
    void insertUser(UserData user) throws DataAccessException;

    // Method to retrieve a user by username
    UserData getUser(String username) throws DataAccessException;

    // Method to clear database
    void clear() throws DataAccessException;

    // Method to hash a password
    String hashPassword(String password);
}
