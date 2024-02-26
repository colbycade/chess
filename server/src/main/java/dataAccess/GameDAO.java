package dataAccess;

import model.GameData;

import java.util.List;

public interface GameDAO {

    // Method to create a new game in the database
    void createGame(GameData game) throws DataAccessException;

    // Method to fetch a game by its ID
    GameData getGame(int gameID) throws DataAccessException;

    // Method to update an existing game's data
    void updateGame(GameData game) throws DataAccessException;

    // Method to list all games
    List<GameData> listGames() throws DataAccessException;
    
}
