package dataAccess;

import exception.DataAccessException;
import model.GameData;

import java.util.Collection;

public interface GameDAO {

    // Method to create a new game in the database given a game name. Returns the game's ID.
    int createGame(String gameName) throws DataAccessException;

    // Method to fetch a game by its ID
    GameData getGame(int gameID) throws DataAccessException;

    // Method to update an existing game's data
    void updateGame(GameData game) throws DataAccessException;

    // Method to list all games
    Collection<GameData> listGames() throws DataAccessException;

    // Method to clear database
    void clear() throws DataAccessException;
}
