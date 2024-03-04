package dataAccess;

import exception.DataAccessException;
import model.GameData;

import java.util.Collection;

public interface GameDAO {

    // Create a new game in the database given a game name. Returns the game's ID.
    Integer createGame(String gameName) throws DataAccessException;

    // Get a game by its ID
    GameData getGame(Integer gameID) throws DataAccessException;

    // Method to update an existing game's data
    void updateGame(GameData game) throws DataAccessException;

    // List all games
    Collection<GameData> listGames() throws DataAccessException;

    // Clear database data related to games
    void clear() throws DataAccessException;
}
