package service;

import dataaccess.*;
import dataaccess.inmemorydatabase.MemoryAuthDAO;
import dataaccess.inmemorydatabase.MemoryUserDAO;
import dataaccess.sqldatabase.MySQLAuthDAO;
import dataaccess.sqldatabase.MySQLUserDAO;
import exception.*;
import model.UserData;
import model.AuthData;
import service.request.LoginRequest;
import service.response.LoginResponse;
import service.request.RegisterRequest;
import service.response.RegisterResponse;
import service.request.LogoutRequest;

import static service.AuthUtil.verifyAuthToken;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService() {
        this.userDAO = new MySQLUserDAO();
        this.authDAO = new MySQLAuthDAO();
    }

    public RegisterResponse register(RegisterRequest request) throws DataAccessException {
        if (request.username() == null || request.password() == null || request.email() == null) {
            throw new BadRequestException("missing required fields");
        }

        // Check if user already exists
        if (userDAO.getUser(request.username()) != null) {
            throw new AlreadyTakenException("user already exists");
        }

        // Create user and auth token
        userDAO.insertUser(new UserData(request.username(), request.password(), request.email()));
        AuthData authData = authDAO.createAuth(request.username());

        return new RegisterResponse(request.username(), authData.authToken());
    }

    public LoginResponse login(LoginRequest request) throws DataAccessException {
        // Check that user exists and password is correct when hashed
        try {
            UserData user = userDAO.getUser(request.username());
            if (user == null) {
                throw new UnauthorizedException("user does not exist");
            }
            String storedHashedPassword = user.password();
            if (!userDAO.isMatch(request.password(), storedHashedPassword)) {
                throw new UnauthorizedException("incorrect password");
            }

            // Create auth token
            AuthData authData = authDAO.createAuth(request.username());
            return new LoginResponse(user.username(), authData.authToken());

        } catch (DataAccessException e) {
            throw new UnauthorizedException(e.getMessage());
        }
    }

    public void logout(LogoutRequest request) throws DataAccessException {
        verifyAuthToken(authDAO, request.authToken());

        // Delete the auth token
        authDAO.deleteAuth(request.authToken());
    }

    public void clearService() throws DataAccessException {
        userDAO.clear();
        authDAO.clear();
    }
}


