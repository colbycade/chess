package service;

import dataAccess.AuthDAO;
import exception.DataAccessException;
import exception.UnauthorizedException;

public class AuthUtil {
    public static void verifyAuthToken(AuthDAO authDAO, String authToken) throws DataAccessException {
        if (authToken == null || authDAO.getAuth(authToken) == null) {
            throw new UnauthorizedException("Unauthorized");
        }
    }
}
