package serviceTests;

import dataAccess.DataAccessException;
import service.UserService;
import dataAccess.MemoryAuthDAO;
import dataAccess.MemoryUserDAO;
import org.junit.jupiter.api.Test;
import service.request.RegisterRequest;
import service.response.RegisterResponse;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Test
    public void testRegisterSuccess() throws DataAccessException {
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        MemoryUserDAO userDAO = new MemoryUserDAO();
        UserService userService = new UserService(userDAO, authDAO);
        RegisterResponse response = userService.register(new RegisterRequest("username", "password", "email"));
        assertNotNull(response);
    }

    @Test
    public void testRegisterFailure() throws DataAccessException {
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        MemoryUserDAO userDAO = new MemoryUserDAO();
        UserService userService = new UserService(userDAO, authDAO);
        userService.register(new RegisterRequest("username", "password", "email"));
        assertThrows(DataAccessException.class,
                () -> userService.register(new RegisterRequest("username", "password", "email")));
    }


}