package dataAccess.inMemoryDatabase;

import exception.BadRequestException;
import exception.DataAccessException;
import dataAccess.UserDAO;
import model.UserData;

import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {
    private final HashMap<String, UserData> users = new HashMap<>();

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
    public void insertUser(UserData user) throws DataAccessException {
        if (user == null) {
            throw new BadRequestException("User data cannot be null.");
        }
        if (users.containsKey(user.username())) {
            throw new BadRequestException("User already exists.");
        }
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) throws BadRequestException {
        if (username == null) {
            throw new BadRequestException("User data cannot be null.");
        }
        return users.get(username);
    }

    @Override
    public void clear() {
        users.clear();
    }

    @Override
    public boolean isMatch(String password, String storedPassword) {
        return (password.equals(storedPassword)); // no hashing for in-memory database
    }
}
