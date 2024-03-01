package dataaccesstests;

import dataaccess.AuthDAO;
import dataaccess.inmemorydatabase.MemoryAuthDAO;
import dataaccess.sqldatabase.MySQLAuthDAO;
import exception.DataAccessException;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuthDAOTests {

    private AuthDAO authDAO;

    @BeforeEach
    public void AuthDAOTests() throws DataAccessException {
        // Default to MemoryUserDAO if the property is not set
        // (edit run config or use -DuserDaoType=mysql in VM options)
        String daoType = System.getProperty("userDaoType", "memory");
        switch (daoType) {
            case "mysql" -> authDAO = new MySQLAuthDAO();
            case "memory" -> {
                authDAO = MemoryAuthDAO.getInstance();
                authDAO.clear(); // prevent data from persisting between tests because MemoryAuthDAO is a singleton
            }
        }
    }

    @Test
    public void testCreateAuth() throws DataAccessException {
        AuthData authCreate = authDAO.createAuth("username");
        assertNotNull(authCreate);
    }

    @Test
    public void testGetAuth() throws DataAccessException {
        AuthData authCreate = authDAO.createAuth("username");
        String authToken = authCreate.authToken();
        AuthData authGet = authDAO.getAuth(authToken);
        assertNotNull(authGet);
    }

    @Test
    public void testDeleteAuth() throws DataAccessException {
        AuthData authCreate = authDAO.createAuth("username");
        String authToken = authCreate.authToken();
        authDAO.deleteAuth(authToken);
        assertNull(authDAO.getAuth(authToken));
    }

    @Test
    public void testClear() throws DataAccessException {
        AuthData authCreate = authDAO.createAuth("username2");
        authDAO.clear();
        assertNull(authDAO.getAuth(authCreate.authToken()));
    }


}
