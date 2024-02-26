package dataAccess;

import model.AuthData;

import java.util.HashMap;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO {
    private HashMap<String, AuthData> authorization;

    public MemoryAuthDAO() {
        authorization = new HashMap<>();
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

    }
}
