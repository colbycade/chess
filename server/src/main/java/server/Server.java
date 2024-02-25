package server;

import handler.*;
import spark.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", RegisterHandler.getInstance());
        Spark.post("/session", LoginHandler.getInstance());
        Spark.delete("/session", LogoutHandler.getInstance());
        Spark.get("/game", ListGamesHandler.getInstance());
        Spark.post("/game", CreateGameHandler.getInstance());
        Spark.put("/game/join", JoinGameHandler.getInstance());
        Spark.delete("/game/clear", ClearApplicationHandler.getInstance());

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
