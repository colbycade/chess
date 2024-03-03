package servicetests;

import chess.ChessGame;
import dataaccess.inmemorydatabase.MemoryAuthDAO;
import dataaccess.inmemorydatabase.MemoryGameDAO;
import exception.BadRequestException;
import exception.DataAccessException;
import exception.UnauthorizedException;
import exception.AlreadyTakenException;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import service.GameService;
import service.request.CreateGameRequest;
import service.request.JoinGameRequest;
import service.request.ListGamesRequest;
import service.response.CreateGameResponse;
import service.response.ListGamesResponse;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTests {
    private GameService gameService;
    // These are dependencies of the GameService used to set up tests
    private MemoryAuthDAO authDAO;
    private MemoryGameDAO gameDAO;

    @BeforeEach
    public void setUp() {
        authDAO = MemoryAuthDAO.getInstance();
        gameDAO = MemoryGameDAO.getInstance();
        authDAO.clear();
        gameDAO.clear();
        gameService = new GameService();
    }

    @Nested
    class CreateGameTest {
        String authToken;
        final String gameName = "my_game";

        @BeforeEach
        public void setUp() throws DataAccessException {
            // Insert a test auth
            authToken = authDAO.createAuth("username").authToken();
        }

        @Test
        public void testCreateGameSuccess() throws DataAccessException {
            CreateGameResponse response = gameService.createGame(new CreateGameRequest(authToken, gameName));
            assertNotNull(response);
            assertNotNull(response.gameID());
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

    @Nested
    class ListGamesTest {
        String authToken;

        @BeforeEach
        public void setUp() throws DataAccessException {
            // Insert a test auth
            authToken = authDAO.createAuth("username").authToken();
        }

        @Test
        public void testListGamesSuccess() throws DataAccessException {
            ListGamesResponse response = gameService.listGames(new ListGamesRequest(authToken));
            assertNotNull(response);
            assertNotNull(response.games());

            // Test that a Collection of ChessGames is returned
            assertInstanceOf(Collection.class, response.games());
            for (Object game : response.games()) {
                assertInstanceOf(ChessGame.class, game);
            }
        }

        @Test
        public void testListGamesFailure() {
            String badAuthToken = "badToken";
            assertThrows(UnauthorizedException.class,
                    () -> gameService.listGames(new ListGamesRequest(badAuthToken)));
        }
    }

    @Nested
    class JoinGameTest {
        String authToken;
        int gameID;
        final String username = "username";
        final String gameName = "my_game";


        @BeforeEach
        public void setUp() throws DataAccessException {
            // Insert a test auth
            authToken = authDAO.createAuth(username).authToken();
            // Create a test game
            gameID = gameDAO.createGame(gameName);
        }

        @Test
        public void testJoinGameSuccess() {
            // Successful join with white
            JoinGameRequest successfulRequest = new JoinGameRequest(authToken, ChessGame.TeamColor.WHITE, gameID);
            assertDoesNotThrow(() -> gameService.joinGame(successfulRequest));

            GameData updatedGame = gameDAO.getGame(gameID);
            assertEquals(username, updatedGame.whiteUsername());
            assertNull(updatedGame.blackUsername());

            // Joining as observer doesn't affect the game data
            JoinGameRequest observerRequest = new JoinGameRequest(authToken, null, gameID);
            assertDoesNotThrow(() -> gameService.joinGame(observerRequest));
            assertEquals(updatedGame, gameDAO.getGame(gameID));
        }

        @Test
        public void testJoinGameFailure() throws DataAccessException {
            // Invalid auth token
            String badAuthToken = "badToken";
            JoinGameRequest invalidAuthRequest = new JoinGameRequest(badAuthToken, ChessGame.TeamColor.WHITE, gameID);
            assertThrows(UnauthorizedException.class, () -> gameService.joinGame(invalidAuthRequest));

            // Color already taken
            // add a game with a white player
            gameDAO.updateGame(new GameData(gameID, username, null, gameName, new ChessGame()));
            // insert a second auth
            String secondAuthToken = authDAO.createAuth("secondUsername").authToken();
            // attempt to join the game with white
            JoinGameRequest colorTakenRequest = new JoinGameRequest(secondAuthToken, ChessGame.TeamColor.WHITE, gameID);
            assertThrows(AlreadyTakenException.class, () -> gameService.joinGame(colorTakenRequest));

            // Non-existent game
            int nonExistentGameID = -1;
            JoinGameRequest nonExistentGameRequest = new JoinGameRequest(authToken, ChessGame.TeamColor.WHITE, nonExistentGameID);
            assertThrows(BadRequestException.class, () -> gameService.joinGame(nonExistentGameRequest));
        }
    }

    @Nested
    class clearServiceTest {
        int gameID;

        @BeforeEach
        public void setUp() throws DataAccessException {
            // Pre-insert data
            gameID = gameDAO.createGame("testGame");
            if (gameDAO.getGame(gameID) == null) {
                throw new DataAccessException("Failed to insert test data");
            }
        }

        @Test
        public void testClearServiceSuccess() {
            assertDoesNotThrow(() -> gameService.clearService());
            // Assert data has been cleared
            assertNull(gameDAO.getGame(gameID));
        }
    }
}