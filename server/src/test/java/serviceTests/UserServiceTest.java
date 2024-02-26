package serviceTests;

import model.*;
import dataAccess.exception.*;
import dataAccess.InMemoryDatabase.MemoryAuthDAO;
import dataAccess.InMemoryDatabase.MemoryUserDAO;
import org.junit.jupiter.api.BeforeAll;
import service.UserService;
import service.request.LoginRequest;
import service.request.RegisterRequest;
import service.response.LoginResponse;
import service.response.RegisterResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

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

    @Nested
    class RegisterTest {
        @Test
        public void testRegisterSuccess() throws DataAccessException {
            RegisterResponse response = userService.register(new RegisterRequest("username", "password", "email"));
            assertNotNull(response);
            assertEquals("username", response.username());
            assertNotNull(response.authToken());
        }

        @Test
        public void testRegisterFailure() throws DataAccessException {
            userService.register(new RegisterRequest("username", "password", "email"));
            assertThrows(AlreadyTakenException.class,
                    () -> userService.register(new RegisterRequest("username", "password", "email")));
            assertThrows(BadRequestException.class,
                    () -> userService.register(new RegisterRequest(null, "password", "email")));
        }
    }

    @Nested
    class LoginTest {
        String username;
        String password;
        String email;

        @BeforeEach
        public void setUp() throws DataAccessException {
            // Insert a test user
            username = "username";
            password = "password";
            email = "email";
            MemoryUserDAO.getInstance().insertUser(new UserData(username, password, email));
        }

        @Test
        public void testLoginSuccess() throws DataAccessException {
            // Attempt to log in with the same credentials
            LoginRequest request = new LoginRequest(username, password);
            LoginResponse response = userService.login(request);

            // Assert that a valid login response is received
            assertNotNull(response);
            assertEquals("username", response.username());
            assertNotNull(response.authToken());
        }

        @Test
        public void testLoginFailure() throws DataAccessException {
            String nonExistentUsername = "nonExistentUser";
            String incorrectPassword = "incorrectPassword";

            // Attempt to log in with username that does not exist
            LoginRequest loginRequestNonExistent = new LoginRequest(nonExistentUsername, password);
            assertThrows(UnauthorizedException.class, () -> userService.login(loginRequestNonExistent));

            // Attempt to log in with incorrect password
            LoginRequest loginRequestIncorrectPassword = new LoginRequest(username, incorrectPassword);
            assertThrows(UnauthorizedException.class, () -> userService.login(loginRequestIncorrectPassword));
        }
    }
}