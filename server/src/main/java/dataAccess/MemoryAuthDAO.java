package dataAccess;

import model.AuthData;

import java.util.ArrayList;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO {
    private ArrayList<AuthData> Authorization;

    public MemoryAuthDAO() {
        Authorization = new ArrayList<>();
    }

    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, username);
        Authorization.add(auth);
        return auth;
    }

    @Override
    public AuthData getAuth(AuthData auth) throws DataAccessException {
        return null;
    }

    @Override
    public void deleteAuth(AuthData auth) throws DataAccessException {

    }
}
