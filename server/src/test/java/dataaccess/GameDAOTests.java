package dataaccess;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import dataaccess.inmemorydb.MemoryGameDAO;
import dataaccess.mysqldb.MySQLGameDAO;
import exception.BadRequestException;
import exception.DataAccessException;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class GameDAOTests {
    
    private GameDAO gameDAO;
    
    @BeforeEach
    public void setUp() throws DataAccessException {
        // Default to MemoryUserDAO if the property is not set
        // (edit run config or use -DuserDaoType=mysql in VM options)
        String daoType = System.getProperty("userDaoType", "memory");
        switch (daoType) {
            case "mysql" -> gameDAO = new MySQLGameDAO();
            case "memory" -> gameDAO = MemoryGameDAO.getInstance();
        }
        gameDAO.clear();
    }
    
    @Test
    public void testCreateGameSuccess() throws DataAccessException {
        Integer gameID = gameDAO.createGame("testGame");
        assertNotNull(gameID);
    }
    
    @Test
    public void testCreateGameFail() {
        assertThrows(BadRequestException.class, () -> gameDAO.createGame(null));
    }
    
    @Test
    public void testGetGameSuccess() throws DataAccessException {
        int gameID = gameDAO.createGame("testGame");
        GameData game = gameDAO.getGame(gameID);
        assertNotNull(game);
    }
    
    @Test
    public void testGetGameFail() throws DataAccessException {
        gameDAO.createGame("testGame");
        assertNull(gameDAO.getGame(-1));
    }
    
    @Test
    public void testUpdateGameSuccess() throws DataAccessException, InvalidMoveException {
        int gameID = gameDAO.createGame("oldGame");
        GameData oldGame = gameDAO.getGame(gameID);
        ChessGame newGame = oldGame.game();
        ChessMove testMove = new ChessMove(new ChessPosition(2, 1), new ChessPosition(3, 1), null);
        newGame.makeMove(testMove);
        GameData updatedGame = new GameData(gameID, "newWhiteUser", "newBlackUser", "newGame", newGame);
        gameDAO.updateGame(updatedGame);
        GameData returnedGame = gameDAO.getGame(gameID);
        assertEquals("newGame", returnedGame.gameName());
    }
    
    @Test
    public void testUpdateGameFail() throws DataAccessException {
        int gameID = gameDAO.createGame("game");
        GameData game = gameDAO.getGame(gameID);
        GameData updatedGame = new GameData(-1, "newWhiteUser", "newBlackUser", "newGame", game.game());
        assertThrows(DataAccessException.class, () -> gameDAO.updateGame(updatedGame));
    }
    
    @Test
    public void testListGamesSuccess() throws DataAccessException {
        gameDAO.createGame("testGame1");
        gameDAO.createGame("testGame2");
        gameDAO.createGame("testGame3");
        gameDAO.createGame("testGame4");
        Collection<GameData> games = gameDAO.listGames();
        assertNotNull(games);
        assertEquals(4, games.size());
    }
    
    @Test
    public void testListGamesFail() throws DataAccessException {
        Collection<GameData> games = gameDAO.listGames();
        assertTrue(games.isEmpty());
    }
    
    @Test
    public void testClear() throws DataAccessException {
        gameDAO.createGame("testGame1");
        gameDAO.createGame("testGame2");
        gameDAO.createGame("testGame3");
        gameDAO.createGame("testGame4");
        gameDAO.clear();
        Collection<GameData> games = gameDAO.listGames();
        assertNotNull(games);
        assertEquals(0, games.size());
    }
    
}
