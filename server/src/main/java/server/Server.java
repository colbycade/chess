package server;

import com.google.gson.Gson;
import dataAccess.AuthDAO;
import dataAccess.GameDAO;
import dataAccess.UserDAO;
import dataAccess.mySQLDatabase.MySQLAuthDAO;
import dataAccess.mySQLDatabase.MySQLGameDAO;
import dataAccess.mySQLDatabase.MySQLUserDAO;
import exception.*;
import handler.*;
import spark.*;

import java.util.Map;

public class Server {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final UserDAO userDAO;

    public Server(AuthDAO authDAO, GameDAO gameDAO, UserDAO userDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
        this.userDAO = userDAO;
    }

    public Server() { // Default to mysql
        this.authDAO = new MySQLAuthDAO();
        this.gameDAO = new MySQLGameDAO();
        this.userDAO = new MySQLUserDAO();
    }

    public int run(int desiredPort) {
        Gson gson = new Gson();
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register endpoints
        Spark.post("/user", new RegisterHandler(authDAO, userDAO));
        Spark.post("/session", new LoginHandler(authDAO, userDAO));
        Spark.delete("/session", new LogoutHandler(authDAO, userDAO));
        Spark.post("/game", new CreateGameHandler(authDAO, gameDAO));
        Spark.get("/game", new ListGamesHandler(authDAO, gameDAO));
        Spark.put("/game", new JoinGameHandler(authDAO, gameDAO));
        Spark.delete("/db", new ClearApplicationHandler(gameDAO, authDAO, userDAO));

        // Setup global exception handlers
        Spark.exception(BadRequestException.class, (e, req, res) -> {
            res.status(400);
            res.type("application/json");
            res.body(gson.toJson(Map.of("message", "Error: bad request")));
        });

        Spark.exception(UnauthorizedException.class, (e, req, res) -> {
            res.status(401);
            res.type("application/json");
            res.body(gson.toJson(Map.of("message", "Error: unauthorized")));
        });

        Spark.exception(AlreadyTakenException.class, (e, req, res) -> {
            res.status(403);
            res.type("application/json");
            res.body(gson.toJson(Map.of("message", "Error: already taken")));
        });

        Spark.exception(Exception.class, (e, req, res) -> {
            res.status(500);
            res.type("application/json");
            res.body(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        });

        Spark.notFound((req, res) -> {
            var msg = String.format("[%s] %s not found", req.requestMethod(), req.pathInfo());
            res.status(500);
            res.type("application/json");
            res.body(gson.toJson(Map.of("message", "Error: " + msg)));
            return res.body();
        });


        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    public int port() {
        return Spark.port();
    }
}
