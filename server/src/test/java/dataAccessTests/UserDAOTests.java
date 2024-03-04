package dataAccessTests;

import dataAccess.UserDAO;
import dataAccess.inMemoryDatabase.MemoryUserDAO;
import dataAccess.mySQLDatabase.MySQLUserDAO;
import exception.DataAccessException;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserDAOTests {

    private UserDAO userDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        // Default to MemoryUserDAO if the property is not set
        // (edit run config or use -DuserDaoType=mysql in VM options)
        String daoType = System.getProperty("userDaoType", "memory");
        switch (daoType) {
            case "mysql" -> userDAO = new MySQLUserDAO();
            case "memory" -> userDAO = MemoryUserDAO.getInstance();
        }
        userDAO.clear();
    }

    @Test
    public void testInsertUser() {
        UserData user = new UserData("username", "password", "email");
        assertDoesNotThrow(() -> userDAO.insertUser(user));
    }

    @Test
    public void testGetUser() throws DataAccessException {
        UserData user = new UserData("username", "password", "email");
        userDAO.insertUser(user);
        UserData userReturned = userDAO.getUser("username");
        assertEquals(userReturned.username(), user.username());
        String encodedPasswordFromStorage = userReturned.password();
        assertTrue(userDAO.isMatch(user.password(), encodedPasswordFromStorage));
    }

    @Test
    public void testClear() throws DataAccessException {
        UserData user = new UserData("username", "password", "email");
        userDAO.insertUser(user);
        userDAO.clear();
        assertNull(userDAO.getUser("username"));
    }

}
