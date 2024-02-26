package service;

import dataAccess.*;
import model.UserData;
import model.AuthData;
import service.request.LoginRequest;
import service.response.LoginResponse;
import service.request.RegisterRequest;
import service.response.RegisterResponse;
import service.request.LogoutRequest;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResponse register(RegisterRequest request) throws DataAccessException {
        // Check if user already exists
        if (userDAO.getUser(request.username()) != null) {
            throw new DataAccessException("User already exists.");
        }

        // Create user and auth token
        UserData userData = new UserData(request.username(), request.password(), request.email());
        userDAO.insertUser(userData);
        AuthData authData = authDAO.createAuth(userData.username());

        return new RegisterResponse(authData.authToken());
    }

    public LoginResponse login(LoginRequest req) {
        return null;
    }

    public void logout(LogoutRequest req) {
    }

}
