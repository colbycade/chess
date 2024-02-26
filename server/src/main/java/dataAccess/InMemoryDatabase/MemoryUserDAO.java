package dataAccess.InMemoryDatabase;

import exception.DataAccessException;
import dataAccess.UserDAO;
import model.UserData;

import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {
    private HashMap<String, UserData> users = new HashMap<>();

    private static MemoryUserDAO instance = null;

    // Private constructor to prevent direct instantiation
    private MemoryUserDAO() {
    }

    // Public method to get the singleton instance
    public static MemoryUserDAO getInstance() {
        if (instance == null) {
            instance = new MemoryUserDAO();
        }
        return instance;
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())) {
            throw new DataAccessException("User already exists.");
        }
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return users.get(username);
    }

    @Override
    public void clear() throws DataAccessException {
        users.clear();
    }
}
