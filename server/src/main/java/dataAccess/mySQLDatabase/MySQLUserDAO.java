package dataAccess.mySQLDatabase;

import dataAccess.DatabaseManager;
import dataAccess.UserDAO;
import exception.BadRequestException;
import exception.DataAccessException;
import model.UserData;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.*;

public class MySQLUserDAO implements UserDAO {

    public MySQLUserDAO() {
        try {
            DatabaseManager.createDatabase();
            try (var conn = DatabaseManager.getConnection()) {
                var statement = """
                        CREATE TABLE IF NOT EXISTS User (
                            username VARCHAR(64) PRIMARY KEY,
                            password_hash VARCHAR(72) NOT NULL,
                            email VARCHAR(330) NOT NULL
                        )""";
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    @Override
    public void insertUser(UserData user) throws DataAccessException {
        if (user == null) {
            throw new BadRequestException("User data cannot be null.");
        }
        String hashedPassword = hashPassword(user.password());
        try (var conn = DatabaseManager.getConnection()) {
            // Check if user already exists
            var statement = "SELECT * FROM User WHERE username = ?";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, user.username());
                var resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    throw new BadRequestException("User already exists.");
                }
            }
            // Add new user
            statement = "INSERT INTO User (username, password_hash, email) VALUES (?, ?, ?)";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, user.username());
                preparedStatement.setString(2, hashedPassword);
                preparedStatement.setString(3, user.email());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        if (username == null) {
            throw new BadRequestException("username cannot be null.");
        }
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM User WHERE username = ?";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, username);
                var resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return new UserData(
                            resultSet.getString("username"),
                            resultSet.getString("password_hash"),
                            resultSet.getString("email")
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "TRUNCATE TABLE User";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public boolean isMatch(String rawPassword, String encodedPasswordFromStorage) {
        if (rawPassword == null || encodedPasswordFromStorage == null) {
            throw new IllegalArgumentException("passwords cannot be null.");
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(rawPassword, encodedPasswordFromStorage);
    }

    private String hashPassword(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }
}
