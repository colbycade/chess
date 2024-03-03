package dataaccess.sqldatabase;

import dataaccess.DatabaseManager;
import dataaccess.UserDAO;
import exception.DataAccessException;
import model.UserData;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.*;

public class MySQLUserDAO implements UserDAO {

    public MySQLUserDAO() throws DataAccessException {
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
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }


    @Override
    public void insertUser(UserData user) throws DataAccessException {
        String hashedPassword = hashPassword(user.password());
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "INSERT INTO User (username, password_hash, email) VALUES (?, ?, ?)";
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
                } else {
                    throw new DataAccessException("User does not exist");
                }
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
    public String hashPassword(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(password);
        return hashedPassword;
    }
}
