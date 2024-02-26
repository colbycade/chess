package dataAccess;

import model.AuthData;

public interface AuthDAO {
    // Method to create a new auth token for a user
    AuthData createAuth(String username) throws DataAccessException;

    // Method to retrieve auth data by token
    AuthData getAuth(AuthData auth) throws DataAccessException;

    // Method to delete an auth token
    void deleteAuth(AuthData auth) throws DataAccessException;
}
