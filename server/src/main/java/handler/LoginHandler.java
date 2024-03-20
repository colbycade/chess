package handler;

import com.google.gson.Gson;
import dataAccess.AuthDAO;
import dataAccess.UserDAO;
import exception.DataAccessException;
import service.UserService;
import model.request.LoginRequest;
import model.response.LoginResponse;
import spark.Request;
import spark.Response;
import spark.Route;

public class LoginHandler implements Route {
    private final UserService userService;
    private final Gson gson = new Gson();

    public LoginHandler(AuthDAO authDAO, UserDAO userDAO) {
        this.userService = new UserService(authDAO, userDAO);
    }

    @Override
    public Object handle(Request req, Response res) throws DataAccessException {
        LoginRequest loginRequest = gson.fromJson(req.body(), LoginRequest.class);
        LoginResponse loginResponse = userService.login(loginRequest);
        res.status(200);
        res.type("application/json");
        return gson.toJson(loginResponse);
    }
}
