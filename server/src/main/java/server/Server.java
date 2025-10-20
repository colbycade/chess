package server;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import dataaccess.mysqldb.MySQLAuthDAO;
import dataaccess.mysqldb.MySQLGameDAO;
import dataaccess.mysqldb.MySQLUserDAO;
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
        
        // Setup WebSocket
        Spark.staticFiles.location("web");
        Spark.webSocketIdleTimeoutMillis(15 * 60 * 1000); // 15 minutes
        Spark.webSocket("/connect", new WebSocketHandler(authDAO, gameDAO));
        
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
