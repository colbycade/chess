package servicetests;

import model.*;
import exception.*;
import dataaccess.inmemorydatabase.MemoryAuthDAO;
import dataaccess.inmemorydatabase.MemoryUserDAO;
import service.UserService;
import service.request.LoginRequest;
import service.request.RegisterRequest;
import service.response.LoginResponse;
import service.response.RegisterResponse;
import service.request.LogoutRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTests {
    private UserService userService;
    // These are dependencies of the UserService used to set up tests
    private MemoryAuthDAO authDAO;
    private MemoryUserDAO userDAO;

    @BeforeEach
    public void setUp() {
        authDAO = MemoryAuthDAO.getInstance();
        userDAO = MemoryUserDAO.getInstance();
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
            userDAO.insertUser(new UserData(username, password, email));
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
        public void testLoginFailure() {
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

    @Nested
    class LogoutTest {
        String username;
        String password;
        String email;
        String authToken;

        @BeforeEach
        public void setUp() throws DataAccessException {
            // Insert a test user
            username = "username";
            password = "password";
            email = "email";
            userDAO.insertUser(new UserData(username, password, email));

            // Add an auth token for the user
            authToken = authDAO.createAuth(username).authToken();
        }

        @Test
        public void testLogoutSuccess() throws DataAccessException {
            // Attempt to log out with the auth token
            LogoutRequest request = new LogoutRequest(authToken);
            userService.logout(request);

            // Assert that the auth token is deleted
            assertNull(authDAO.getAuth(authToken));
        }

        @Test
        public void testLogoutFailure() {
            String invalidAuthToken = "invalidAuthToken";

            // Attempt to log out with an invalid auth token
            LogoutRequest request = new LogoutRequest(invalidAuthToken);
            assertThrows(UnauthorizedException.class, () -> userService.logout(request));
        }
    }

    @Nested
    class clearServiceTest {
        String authToken;
        final String username = "testUsername";

        @BeforeEach
        public void setUp() throws DataAccessException {
            // Pre-insert data
            userDAO.insertUser(new UserData(username, "testPassword", "testEmail"));
            authToken = authDAO.createAuth(username).authToken();
            if (userDAO.getUser(username) == null || authDAO.getAuth(authToken) == null) {
                throw new DataAccessException("Failed to insert test data");
            }
        }

        @Test
        public void testClearServiceSuccess() {
            assertDoesNotThrow(() -> userService.clearService());
            // Assert data has been cleared
            assertNull(userDAO.getUser(username));
            assertNull(authDAO.getAuth(authToken));
        }
    }
}