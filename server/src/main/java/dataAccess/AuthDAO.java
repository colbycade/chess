package dataAccess;

import exception.DataAccessException;
import model.AuthData;

public interface AuthDAO {
    // Create a new auth token for a user
    AuthData createAuth(String username) throws DataAccessException;

    // Retrieve auth data by token
    AuthData getAuth(String authToken) throws DataAccessException;

    // Delete an auth token
    void deleteAuth(String authToken) throws DataAccessException;

    // Clear database data related to authentication
    void clear() throws DataAccessException;
}
