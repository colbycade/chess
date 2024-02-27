package service;

import dataAccess.*;
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
        userDAO.createUser(new UserData(request.username(), request.password(), request.email()));
        AuthData authData = authDAO.createAuth(request.username());

        return new RegisterResponse(request.username(), authData.authToken());
    }

    public LoginResponse login(LoginRequest request) throws DataAccessException {
        // Check that user exists and password is correct
        UserData user = userDAO.getUser(request.username());
        if (user == null || !user.password().equals(request.password())) {
            throw new UnauthorizedException("unauthorized");
        }

        // Create auth token
        AuthData authData = authDAO.createAuth(request.username());

        return new LoginResponse(authData.authToken(), user.username());
    }

    public void logout(LogoutRequest request) throws DataAccessException {
        verifyAuthToken(authDAO, request.authToken());

        // Delete the auth token
        authDAO.deleteAuth(request.authToken());
    }

}


