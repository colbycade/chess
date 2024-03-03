package dataaccess;

import exception.DataAccessException;
import model.*;

public interface UserDAO {
    // Insert a new user into the database
    void insertUser(UserData user) throws DataAccessException;

    // Retrieve a user by username, returns null if user not found
    UserData getUser(String username) throws DataAccessException;

    // Clear database
    void clear() throws DataAccessException;

    // Verify the encoded password obtained from storage matches the submitted raw password after it too is encoded
    boolean isMatch(String rawPassword, String encodedPasswordFromStorage);
}
