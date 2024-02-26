package dataAccess;

import model.*;

public interface UserDAO {
    // Method to insert a new user into the database
    void insertUser(UserData user) throws DataAccessException;

    // Method to retrieve a user by username
    UserData getUser(String username) throws DataAccessException;
}
