package serviceTests;

import dataAccess.exception.AlreadyTakenException;
import dataAccess.exception.DataAccessException;
import dataAccess.InMemoryDatabase.MemoryAuthDAO;
import dataAccess.InMemoryDatabase.MemoryUserDAO;
import service.UserService;
import service.request.RegisterRequest;
import service.response.RegisterResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private UserService userService;

    @BeforeEach
    public void setUp() throws DataAccessException {
        MemoryAuthDAO authDAO = MemoryAuthDAO.getInstance();
        MemoryUserDAO userDAO = MemoryUserDAO.getInstance();
        authDAO.clear();
        userDAO.clear();
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    public void testRegisterSuccess() throws DataAccessException {
        RegisterResponse response = userService.register(new RegisterRequest("username", "password", "email"));
        assertNotNull(response);
    }

    @Test
    public void testRegisterFailure() throws DataAccessException {
        userService.register(new RegisterRequest("username", "password", "email"));
        assertThrows(AlreadyTakenException.class,
                () -> userService.register(new RegisterRequest("username", "password", "email")));
    }


}