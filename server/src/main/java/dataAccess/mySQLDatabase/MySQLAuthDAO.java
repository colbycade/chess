package dataAccess.mySQLDatabase;

import dataAccess.AuthDAO;
import dataAccess.DatabaseManager;
import exception.BadRequestException;
import exception.DataAccessException;
import model.AuthData;

import java.sql.SQLException;
import java.util.UUID;

public class MySQLAuthDAO implements AuthDAO {

    public MySQLAuthDAO() {
        try {
            DatabaseManager.createDatabase();
            try (var conn = DatabaseManager.getConnection()) {
                var statement = """
                        CREATE TABLE IF NOT EXISTS Auth (
                            authToken VARCHAR(36) PRIMARY KEY,
                            username VARCHAR(64) NOT NULL
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
    public AuthData createAuth(String username) throws DataAccessException {
        if (username == null) {
            throw new BadRequestException("username cannot be null.");
        }
        String authToken = UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, username);

        try (var conn = DatabaseManager.getConnection()) {
            var statement = "INSERT INTO Auth (authToken, username) VALUES (?, ?)";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, authToken);
                preparedStatement.setString(2, username);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        return auth;
    }

    @Override
    public AuthData getAuth(String authToken) throws BadRequestException {
        if (authToken == null) {
            throw new BadRequestException("authToken cannot be null.");
        }
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM Auth WHERE authToken = ?";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, authToken);
                var resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    var username = resultSet.getString("username");
                    return new AuthData(authToken, username);
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        if (authToken == null) {
            throw new BadRequestException("authToken cannot be null.");
        }
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "DELETE FROM Auth WHERE authToken = ?";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, authToken);
                preparedStatement.executeUpdate();
            }
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "TRUNCATE TABLE Auth";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}
