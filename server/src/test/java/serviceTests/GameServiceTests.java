package serviceTests;

import chess.ChessGame;
import dataAccess.InMemoryDatabase.MemoryAuthDAO;
import dataAccess.InMemoryDatabase.MemoryGameDAO;
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

    @Nested
    class ListGamesTest {
        String authToken;

        @BeforeEach
        public void setUp() throws DataAccessException {
            // Insert a test auth
            authToken = MemoryAuthDAO.getInstance().createAuth("username").authToken();
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
        String username = "username";
        String gameName = "my_game";


        @BeforeEach
        public void setUp() throws DataAccessException {
            // Insert a test auth
            authToken = MemoryAuthDAO.getInstance().createAuth(username).authToken();
            // Create a test game
            gameID = MemoryGameDAO.getInstance().createGame(gameName);
        }

        @Test
        public void testJoinGameSuccess() {
            // Successful join with white
            JoinGameRequest successfulRequest = new JoinGameRequest(authToken, ChessGame.TeamColor.WHITE, gameID);
            assertDoesNotThrow(() -> gameService.joinGame(successfulRequest));

            GameData updatedGame = MemoryGameDAO.getInstance().getGame(gameID);
            assertEquals(username, updatedGame.whiteUsername());
            assertNull(updatedGame.blackUsername());

            // Joining as observer doesn't affect the game data
            JoinGameRequest observerRequest = new JoinGameRequest(authToken, null, gameID);
            assertDoesNotThrow(() -> gameService.joinGame(observerRequest));
            assertEquals(updatedGame, MemoryGameDAO.getInstance().getGame(gameID));
        }

        @Test
        public void testJoinGameFailure() throws DataAccessException {
            // Invalid auth token
            String badAuthToken = "badToken";
            JoinGameRequest invalidAuthRequest = new JoinGameRequest(badAuthToken, ChessGame.TeamColor.WHITE, gameID);
            assertThrows(UnauthorizedException.class, () -> gameService.joinGame(invalidAuthRequest));

            // Color already taken
            // add a game with a white player
            MemoryGameDAO.getInstance().updateGame(new GameData(gameID, username, null, gameName, new ChessGame()));
            // insert a second auth
            String secondAuthToken = MemoryAuthDAO.getInstance().createAuth("secondUsername").authToken();
            // attempt to join the game with white
            JoinGameRequest colorTakenRequest = new JoinGameRequest(secondAuthToken, ChessGame.TeamColor.WHITE, gameID);
            assertThrows(AlreadyTakenException.class, () -> gameService.joinGame(colorTakenRequest));

            // Non-existent game
            int nonExistentGameID = -1;
            JoinGameRequest nonExistentGameRequest = new JoinGameRequest(authToken, ChessGame.TeamColor.WHITE, nonExistentGameID);
            assertThrows(BadRequestException.class, () -> gameService.joinGame(nonExistentGameRequest));
        }
    }

}