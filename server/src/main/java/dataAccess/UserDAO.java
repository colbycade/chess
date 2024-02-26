package dataAccess;

import exception.DataAccessException;
import model.*;

public interface UserDAO {
    // Method to insert a new user into the database
    void createUser(UserData user) throws DataAccessException;

    // Method to retrieve a user by username
    UserData getUser(String username) throws DataAccessException;

    // Method to clear database
    void clear() throws DataAccessException;
}
