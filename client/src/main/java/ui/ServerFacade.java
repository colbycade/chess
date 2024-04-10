package ui;

import chess.ChessGame;
import model.AuthData;
import model.request.JoinGameRequest;
import model.request.LoginRequest;
import model.request.RegisterRequest;
import model.response.CreateGameResponse;
import model.response.ListGamesResponse;
import model.response.LoginResponse;
import model.response.RegisterResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;


public class ServerFacade {
    
    private final String url;
    private final Integer port;
    private AuthData authData = null;
    private HttpCommunicator httpCommunicator;
    private WebSocketCommunicator wsCommunicator;
    
    public ServerFacade(Integer port) {
        this.url = null; // not needed
        this.port = port;
        httpCommunicator = new HttpCommunicator(port);
    }
    
    // If given url, extract port from url
    public ServerFacade(String url) {
        this.url = url;
        this.port = extractPort(url);
        httpCommunicator = new HttpCommunicator(port);
    }
    
    public String getAuthToken() {
        return authData != null ? authData.authToken() : null;
    }
    
    public void register(String username, String password, String email) throws ResponseException {
        try {
            RegisterRequest requestBody = new RegisterRequest(username, password, email);
            RegisterResponse responseBody = httpCommunicator.sendPostRequest("/user", null, requestBody,
                    RegisterResponse.class);
            this.authData = new AuthData(responseBody.authToken(), responseBody.username());
        } catch (IOException | URISyntaxException e) {
            throw new ResponseException("Registration failed. Error: " + e.getMessage());
        }
    }
    
    public void login(String username, String password) throws ResponseException {
        try {
            LoginRequest requestBody = new LoginRequest(username, password);
            LoginResponse responseBody = httpCommunicator.sendPostRequest("/session", null, requestBody,
                    LoginResponse.class);
            this.authData = new AuthData(responseBody.authToken(), responseBody.username());
        } catch (IOException | URISyntaxException e) {
            throw new ResponseException("Login failed. Error: " + e.getMessage());
        }
    }
    
    // POST-LOGIN COMMANDS
    
    public CreateGameResponse createGame(String authToken, String gameName) throws ResponseException {
        try {
            Map<String, String> requestBody = Map.of("gameName", gameName);
            return httpCommunicator.sendPostRequest("/game", authToken, requestBody, CreateGameResponse.class);
        } catch (IOException | URISyntaxException e) {
            throw new ResponseException("Failed to create game. Error: " + e.getMessage());
        }
    }
    
    public ListGamesResponse listGames(String authToken) throws ResponseException {
        try {
            return httpCommunicator.sendGetRequest("/game", authToken, ListGamesResponse.class);
        } catch (IOException | URISyntaxException e) {
            throw new ResponseException("Failed to list games. Error: " + e.getMessage());
        }
    }
    
    public void joinGame(String authToken, ChessGame.TeamColor clientColor, Integer gameID) throws ResponseException {
        try {
            JoinGameRequest requestBody = new JoinGameRequest(authToken, clientColor, gameID);
            httpCommunicator.sendPutRequest("/game", authToken, requestBody);
        } catch (IOException | URISyntaxException e) {
            throw new ResponseException("Failed to join game. Error: " + e.getMessage());
        }
    }
    
    public void observeGame(String authToken, Integer gameID) throws ResponseException {
        try {
            JoinGameRequest requestBody = new JoinGameRequest(authToken, null, gameID);
            httpCommunicator.sendPutRequest("/game", authToken, requestBody);
        } catch (IOException | URISyntaxException e) {
            throw new ResponseException("Failed to join game. Error: " + e.getMessage());
        }
    }
    
    public void logout() throws ResponseException {
        try {
            httpCommunicator.sendDeleteRequest("/session", getAuthToken());
            authData = null;
        } catch (IOException | URISyntaxException e) {
            throw new ResponseException("Failed to logout. Error: " + e.getMessage());
        }
    }
    
    
    // HELPER METHODS
    
    private int extractPort(String url) {
        try {
            return new URI(url).getPort();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
