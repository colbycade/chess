package service;

import dataAccess.*;
import dataAccess.exception.*;
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
        if (request.username() == null || request.password() == null || request.email() == null) {
            throw new BadRequestException("bad request");
        }

        // Check if user already exists
        if (userDAO.getUser(request.username()) != null) {
            throw new AlreadyTakenException("already taken");
        }

        // Create user and auth token
        userDAO.insertUser(new UserData(request.username(), request.password(), request.email()));
        AuthData authData = authDAO.createAuth(request.username());

        return new RegisterResponse(request.username(), authData.authToken());
    }

    public LoginResponse login(LoginRequest request) throws DataAccessException {
        // Check that user exists
        UserData user = userDAO.getUser(request.username());
        if (user == null) {
            throw new DataAccessException("User does not exist.");
        }

        // Create auth token
        AuthData authData = authDAO.createAuth(request.username());

        return new LoginResponse(authData.authToken(), user.username());
    }

    public void logout(LogoutRequest req) throws DataAccessException {
        authDAO.deleteAuth(req.authToken());
    }

}
