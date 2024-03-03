package dataAccessTests;

import chess.*;
import dataaccess.GameDAO;
import dataaccess.inmemorydatabase.MemoryGameDAO;
import dataaccess.sqldatabase.MySQLGameDAO;
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
    public void testCreateGame() throws DataAccessException {
        Integer gameID = gameDAO.createGame("testGame");
        assertNotNull(gameID);
    }

    @Test
    public void testGetGame() throws DataAccessException {
        int gameID = gameDAO.createGame("testGame");
        GameData game = gameDAO.getGame(gameID);
        assertNotNull(game);
        System.out.println(game);
    }

    @Test
    public void testUpdateGame() throws DataAccessException, InvalidMoveException {
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

    @Test
    public void testListGames() throws DataAccessException {
        gameDAO.createGame("testGame1");
        gameDAO.createGame("testGame2");
        gameDAO.createGame("testGame3");
        gameDAO.createGame("testGame4");
        Collection<GameData> games = gameDAO.listGames();
        assertNotNull(games);
        assertEquals(4, games.size());
    }
}
