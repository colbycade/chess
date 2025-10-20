package handler;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import service.UserService;
import model.request.LogoutRequest;
import spark.Request;
import spark.Response;
import spark.Route;

public class LogoutHandler implements Route {
    private final UserService userService;
    
    public LogoutHandler(AuthDAO authDAO, UserDAO userDAO) {
        this.userService = new UserService(authDAO, userDAO);
    }
    
    @Override
    public Object handle(Request req, Response res) throws Exception {
        LogoutRequest logoutRequest = new LogoutRequest(req.headers("authorization"));
        userService.logout(logoutRequest);
        res.status(200);
        res.type("application/json");
        return "{}"; // No response body
    }
}
