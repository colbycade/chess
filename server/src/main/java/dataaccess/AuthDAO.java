package dataaccess;

import exception.DataAccessException;
import model.AuthData;

public interface AuthDAO {
    // Method to create a new auth token for a user
    AuthData createAuth(String username) throws DataAccessException;

    // Method to retrieve auth data by token
    AuthData getAuth(String authToken);

    // Method to delete an auth token
    void deleteAuth(String authToken);

    // Method to clear database
    void clear();
}
