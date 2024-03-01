package dataaccesstests;

import dataaccess.UserDAO;
import dataaccess.inmemorydatabase.MemoryGameDAO;
import dataaccess.inmemorydatabase.MemoryUserDAO;
import dataaccess.sqldatabase.MySQLUserDAO;
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
            case "memory" -> {
                userDAO = MemoryUserDAO.getInstance();
                userDAO.clear(); // prevent data from persisting between tests because MemoryUserDAO is a singleton
            }
        }
    }

    @Test
    public void testUserDAO() throws DataAccessException {
        UserData userInserted = new UserData("username", "password", "email");
        assertDoesNotThrow(() -> userDAO.insertUser(userInserted));
        UserData userReturned = userDAO.getUser("username");
        assertEquals(userReturned.username(), userInserted.username());
        assertEquals(userReturned.password(), userDAO.hashPassword(userInserted.password()));
    }
}
