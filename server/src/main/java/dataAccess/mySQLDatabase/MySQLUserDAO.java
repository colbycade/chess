package dataAccess.mySQLDatabase;

import dataAccess.DatabaseManager;
import dataAccess.UserDAO;
import exception.DataAccessException;
import model.UserData;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.*;

public class MySQLUserDAO implements UserDAO {

    public MySQLUserDAO() {
        MySQLUtil.initializeDatabase(); // Ensure User table has been created
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
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        MySQLUtil.clearDatabase();
    }

    @Override
    public boolean isMatch(String rawPassword, String encodedPasswordFromStorage) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(rawPassword, encodedPasswordFromStorage);
    }

    private String hashPassword(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }
}
