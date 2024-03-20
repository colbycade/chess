package clientTests;

import dataAccess.inMemoryDatabase.MemoryAuthDAO;
import dataAccess.inMemoryDatabase.MemoryGameDAO;
import dataAccess.inMemoryDatabase.MemoryUserDAO;
import model.UserData;
import model.response.CreateGameResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Server;
import ui.ResponseException;
import ui.ServerFacade;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    MemoryAuthDAO authDAO = MemoryAuthDAO.getInstance();
    MemoryGameDAO gameDAO = MemoryGameDAO.getInstance();
    MemoryUserDAO userDAO = MemoryUserDAO.getInstance();

    @BeforeAll
    public static void init() {
        // Start server using in-memory database for testing
        server = new Server(MemoryAuthDAO.getInstance(), MemoryGameDAO.getInstance(), MemoryUserDAO.getInstance());
        int port = server.run(8080);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearData() {
        authDAO.clear();
        gameDAO.clear();
        userDAO.clear();
    }

    @Test
    void registerSuccess() {
        assertDoesNotThrow(() -> facade.register("player1", "password", "p1@email.com"));
        assertNotNull(facade.getAuthToken());
    }

    @Test
    void registerFail() throws Exception {
        // Invalid inputs throws exception
        assertThrows(ResponseException.class, () -> facade.register(null, null, null));
        // Duplicate username throws exception
        facade.register("player1", "password", "p1@email.com");
        assertThrows(ResponseException.class, () -> facade.register("player1", "password", "p1@email.com"));
    }

    @Test
    void loginSuccess() throws Exception {
        // Insert test user into database
        userDAO.insertUser(new UserData("player1", "password", "p1@email.com"));

        // Login successful
        assertDoesNotThrow(() -> facade.login("player1", "password"));
        assertNotNull(facade.getAuthToken());
    }

    @Test
    void loginFail() {
        // Login unregistered user throws exception
        assertThrows(ResponseException.class, () -> facade.login("unknown_player", "wrong_password"));
    }

    @Test
    void createGameSuccess() throws Exception {
        // Insert test auth into database
        String testAuth = authDAO.createAuth("player1").authToken();

        // Create game successful
        Integer gameID = assertDoesNotThrow(() -> {
            CreateGameResponse response = facade.createGame(testAuth, "game1");
            return response.gameID();
        });
        assertNotNull(gameID);
    }

    @Test
    void createGameFail() throws Exception {
        // Create game without auth throws exception
        assertThrows(ResponseException.class, () -> facade.createGame(null, "game1"));
    }
}