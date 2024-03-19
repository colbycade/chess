package ui;

import chess.ChessGame;
import model.AuthData;
import model.GameData;

import java.util.Collection;

public class ServerFacade {

    private Integer port;
    private AuthData authData = null;

    public ServerFacade(Integer port) {
        this.port = port;
    }

    public void displayHelp() {
        if (authData == null) {
            System.out.println(preLoginHelp);
        } else {
            System.out.println(postLoginHelp);
        }
    }

    private final String preLoginHelp = """
                register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                login <USERNAME> <PASSWORD> - to play chess
                quit - to quit
                help - with possible commands
            """;

    private final String postLoginHelp = """
                create <NAME> - a game
                list - all games
                join <gameID> [WHITE|BLACK] - to join a game
                observe <gameID> - a game
                logout - when you are done playing chess
                quit - to quit
                help - with possible commands
            """;

    public AuthData register(String username, String password, String email) {
        return null;
    }

    public AuthData login(String username, String password) {
        return null;
    }

    public void logout() {
        authData = null;
    }

    public GameData createGame(String authToken, String gameName) {
        return null;
    }

    public Collection<GameData> listGames(String authToken) {
        return null;
    }

    public void joinGame(String authToken, ChessGame.TeamColor clientColor, Integer gameID) {
    }

    public void observeGame(String authToken, Integer gameID) {
    }


}
