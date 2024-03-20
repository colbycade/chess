package clientTests;

import dataAccess.inMemoryDatabase.MemoryAuthDAO;
import dataAccess.inMemoryDatabase.MemoryGameDAO;
import dataAccess.inMemoryDatabase.MemoryUserDAO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Server;
import ui.ResponseException;
import ui.ServerFacade;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        // Start server using in-memory database for testing
        MemoryAuthDAO authDAO = MemoryAuthDAO.getInstance();
        MemoryGameDAO gameDAO = MemoryGameDAO.getInstance();
        MemoryUserDAO userDAO = MemoryUserDAO.getInstance();
        server = new Server(authDAO, gameDAO, userDAO);
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
        MemoryAuthDAO.getInstance().clear();
        MemoryGameDAO.getInstance().clear();
        MemoryUserDAO.getInstance().clear();
    }

    @Test
    void registerSuccess() {
        assertDoesNotThrow(() -> facade.register("player1", "password", "p1@email.com"));
    }

    @Test
    void registerFail() throws Exception {
        // test invalid inputs throws exception
        assertThrows(ResponseException.class, () -> facade.register(null, null, null));
        // test duplicate username throws exception
        facade.register("player1", "password", "p1@email.com");
        assertThrows(ResponseException.class, () -> facade.register("player1", "password", "p1@email.com"));
    }
}