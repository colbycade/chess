package dataaccess.sqldatabase;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DatabaseManager;
import dataaccess.GameDAO;
import exception.DataAccessException;
import model.GameData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class MySQLGameDAO implements GameDAO {

    private final Gson gson;

    public MySQLGameDAO() throws DataAccessException {
        gson = new Gson();
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = """
                    CREATE TABLE IF NOT EXISTS Game (
                        game_id INT PRIMARY KEY AUTO_INCREMENT,
                        white_username VARCHAR(64),
                        black_username VARCHAR(64),
                        game_name VARCHAR(64),
                        game_data BLOB
                    )""";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public Integer createGame(String gameName) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "INSERT INTO Game (game_name, game_data) VALUES (?, ?)";
            try (var preparedStatement = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, gameName);
                preparedStatement.setString(2, gson.toJson(new ChessGame()));
                preparedStatement.executeUpdate();
                var resultSet = preparedStatement.getGeneratedKeys();
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                } else {
                    throw new DataAccessException("Failed to create game");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM Game WHERE game_id = ?";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setInt(1, gameID);
                var resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return new GameData(
                            resultSet.getInt("game_id"),
                            resultSet.getString("white_username"),
                            resultSet.getString("black_username"),
                            resultSet.getString("game_name"),
                            gson.fromJson(resultSet.getString("game_data"), ChessGame.class)
                    );
                } else {
                    throw new DataAccessException("Game does not exist");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = """
                    UPDATE Game SET
                        white_username = ?,
                        black_username = ?,
                        game_name = ?,
                        game_data = ?
                        WHERE game_id = ?
                    """;
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, game.whiteUsername());
                preparedStatement.setString(2, game.blackUsername());
                preparedStatement.setString(3, game.gameName());
                preparedStatement.setString(4, gson.toJson(game.game()));
                preparedStatement.setInt(5, game.gameID());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        ArrayList<GameData> games = new ArrayList<>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM Game";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                var resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    games.add(new GameData(
                            resultSet.getInt("game_id"),
                            resultSet.getString("white_username"),
                            resultSet.getString("black_username"),
                            resultSet.getString("game_name"),
                            gson.fromJson(resultSet.getString("game_data"), ChessGame.class)
                    ));
                }
                return games;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "TRUNCATE TABLE Game";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }


}
