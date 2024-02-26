package serviceTests;

import dataAccess.InMemoryDatabase.MemoryAuthDAO;
import dataAccess.InMemoryDatabase.MemoryGameDAO;
import exception.BadRequestException;
import exception.DataAccessException;
import exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import service.GameService;
import service.request.CreateGameRequest;
import service.response.CreateGameResponse;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GameServiceTests {
    private GameService gameService;

    @BeforeEach
    public void setUp() throws DataAccessException {
        MemoryAuthDAO authDAO = MemoryAuthDAO.getInstance();
        MemoryGameDAO gameDAO = MemoryGameDAO.getInstance();
        authDAO.clear();
        gameDAO.clear();
        gameService = new GameService(gameDAO, authDAO);
    }

    @Nested
    class CreateGameTest {
        String authToken;
        String gameName = "my_game";

        @BeforeEach
        public void setUp() throws DataAccessException {
            // Insert a test auth
            authToken = MemoryAuthDAO.getInstance().createAuth("username").authToken();
        }

        @Test
        public void testCreateGameSuccess() throws DataAccessException {
            CreateGameResponse response = gameService.createGame(new CreateGameRequest(authToken, gameName));
            assertNotNull(response);
            assertNotNull(response.gameId());
        }

        @Test
        public void testCreateGameFailure() {
            String badAuthToken = "badToken";

            assertThrows(UnauthorizedException.class,
                    () -> gameService.createGame(new CreateGameRequest(badAuthToken, gameName)));
            assertThrows(BadRequestException.class,
                    () -> gameService.createGame(new CreateGameRequest(authToken, null)));
        }
    }
}