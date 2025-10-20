package handler;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import exception.DataAccessException;
import service.UserService;
import model.request.RegisterRequest;
import model.response.RegisterResponse;
import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Route;

public class RegisterHandler implements Route {
    private final UserService userService;
    private final Gson gson = new Gson();
    
    public RegisterHandler(AuthDAO authDAO, UserDAO userDAO) {
        this.userService = new UserService(authDAO, userDAO);
    }
    
    @Override
    public Object handle(Request req, Response res) throws DataAccessException {
        RegisterRequest registerRequest = gson.fromJson(req.body(), RegisterRequest.class);
        RegisterResponse registerResponse = userService.register(registerRequest);
        res.status(200);
        res.type("application/json");
        return gson.toJson(registerResponse);
    }
    
}
