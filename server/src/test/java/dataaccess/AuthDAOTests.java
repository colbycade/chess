package dataaccess;

import dataaccess.inmemorydb.MemoryAuthDAO;
import dataaccess.mysqldb.MySQLAuthDAO;
import exception.BadRequestException;
import exception.DataAccessException;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuthDAOTests {
    
    private AuthDAO authDAO;
    
    @BeforeEach
    public void setUp() throws DataAccessException {
        // Default to MemoryUserDAO if the property is not set
        // (edit run config or use -DuserDaoType=mysql in VM options)
        String daoType = System.getProperty("userDaoType", "memory");
        switch (daoType) {
            case "mysql" -> authDAO = new MySQLAuthDAO();
            case "memory" -> authDAO = MemoryAuthDAO.getInstance();
        }
        authDAO.clear();
    }
    
    @Test
    public void testCreateAuthSuccess() throws DataAccessException {
        AuthData authCreate = authDAO.createAuth("username");
        assertNotNull(authCreate);
    }
    
    @Test
    public void testCreateAuthFail() throws DataAccessException {
        AuthData authCreate = authDAO.createAuth("username");
        assertNotNull(authCreate);
    }
    
    @Test
    public void testGetAuthSuccess() throws DataAccessException {
        AuthData authCreate = authDAO.createAuth("username");
        String authToken = authCreate.authToken();
        AuthData authGet = authDAO.getAuth(authToken);
        assertNotNull(authGet);
    }
    
    @Test
    public void testGetAuthFail() throws DataAccessException {
        String badToken = "bad";
        authDAO.createAuth("username");
        AuthData authGet = authDAO.getAuth(badToken);
        assertNull(authGet);
    }
    
    @Test
    public void testDeleteAuthSuccess() throws DataAccessException {
        AuthData authCreate = authDAO.createAuth("username");
        String authToken = authCreate.authToken();
        authDAO.deleteAuth(authToken);
        assertNull(authDAO.getAuth(authToken));
    }
    
    @Test
    public void testDeleteAuthFail() throws DataAccessException {
        authDAO.createAuth("username");
        assertThrows(BadRequestException.class, () -> authDAO.deleteAuth(null));
    }
    
    @Test
    public void testClear() throws DataAccessException {
        AuthData authCreate = authDAO.createAuth("username");
        authDAO.clear();
        assertNull(authDAO.getAuth(authCreate.authToken()));
    }
    
}
