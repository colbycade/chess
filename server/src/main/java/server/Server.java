package server;

import com.google.gson.Gson;
import exception.*;
import handler.*;
import spark.*;

import java.util.Map;

public class Server {
    public int run(int desiredPort) {
        Gson gson = new Gson();
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", RegisterHandler.getInstance());
        Spark.post("/session", LoginHandler.getInstance());
        Spark.delete("/session", LogoutHandler.getInstance());
        Spark.post("/game", CreateGameHandler.getInstance());
        Spark.get("/game", ListGamesHandler.getInstance());
        Spark.put("/game/join", JoinGameHandler.getInstance());
        Spark.delete("/game/clear", ClearApplicationHandler.getInstance());

        // Setup global exception handlers
        Spark.exception(BadRequestException.class, (e, req, res) -> {
            res.status(400);
            res.body(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        });

        Spark.exception(UnauthorizedException.class, (e, req, res) -> {
            res.status(401);
            res.body(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        });

        Spark.exception(AlreadyTakenException.class, (e, req, res) -> {
            res.status(403);
            res.body(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        });

        Spark.exception(Exception.class, (e, req, res) -> {
            res.status(500);
            res.body(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
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
