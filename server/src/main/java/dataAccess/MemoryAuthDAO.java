package dataAccess;

import handler.RegisterHandler;
import model.AuthData;

import java.util.HashMap;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO {
    private HashMap<String, AuthData> authorization = new HashMap<>();

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
        String authToken = UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, username);
        if (authorization.containsKey(authToken)) {
            throw new DataAccessException("AuthToken already exists.");
        }
        authorization.put(authToken, auth);
        return auth;
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return authorization.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        authorization.remove(authToken);
    }
}
