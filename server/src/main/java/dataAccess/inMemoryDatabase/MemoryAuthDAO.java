package dataAccess.inMemoryDatabase;

import dataAccess.AuthDAO;
import exception.BadRequestException;
import exception.DataAccessException;
import model.AuthData;

import java.util.HashMap;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO {
    private final HashMap<String, AuthData> authorization = new HashMap<>();

    private static MemoryAuthDAO instance = null;

    // Private constructor to prevent direct instantiation
    private MemoryAuthDAO() {
    }

    // Public method to get the singleton instance
    public static MemoryAuthDAO getInstance() {
        if (instance == null) {
            instance = new MemoryAuthDAO();
        }
        return instance;
    }

    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        if (username == null) {
            throw new BadRequestException("username cannot be null.");
        }
        String authToken = UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, username);
        if (authorization.containsKey(authToken)) {
            throw new DataAccessException("AuthToken already exists.");
        }
        authorization.put(authToken, auth);
        return auth;
    }

    @Override
    public AuthData getAuth(String authToken) throws BadRequestException {
        if (authToken == null) {
            throw new BadRequestException("authToken cannot be null.");
        }
        return authorization.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws BadRequestException {
        if (authToken == null) {
            throw new BadRequestException("authToken cannot be null.");
        }
        authorization.remove(authToken);
    }

    @Override
    public void clear() {
        authorization.clear();
    }
}
