package ui;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.request.JoinGameRequest;
import model.request.LoginRequest;
import model.request.RegisterRequest;
import model.response.CreateGameResponse;
import model.response.ListGamesResponse;
import model.response.LoginResponse;
import model.response.RegisterResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static ui.EscapeSequences.*;


public class ServerFacade {

    private final String url;
    private final Integer port;
    private AuthData authData = null;

    public ServerFacade(String url) {
        this.url = url;
        this.port = extractPort(url);
    }

    public ServerFacade(Integer port) {
        this.url = null;
        this.port = port;
    }

    public String getAuthToken() {
        return authData != null ? authData.authToken() : null;
    }

    public boolean isLoggedIn() {
        return authData != null;
    }

    public void displayHelp() {
        String preLoginHelp =
                SET_TEXT_COLOR_BLUE + "   register <USERNAME> <PASSWORD> <EMAIL> " + SET_TEXT_COLOR_MAGENTA + "- to create an account\n" +
                        SET_TEXT_COLOR_BLUE + "   login <USERNAME> <PASSWORD> " + SET_TEXT_COLOR_MAGENTA + "- to play chess\n" +
                        SET_TEXT_COLOR_BLUE + "   quit " + SET_TEXT_COLOR_MAGENTA + "- to quit\n" +
                        SET_TEXT_COLOR_BLUE + "   help " + SET_TEXT_COLOR_MAGENTA + "- with possible commands\n" + RESET_ALL;
        String postLoginHelp =
                SET_TEXT_COLOR_BLUE + "   create <NAME> " + SET_TEXT_COLOR_MAGENTA + "- a game\n" +
                        SET_TEXT_COLOR_BLUE + "   list " + SET_TEXT_COLOR_MAGENTA + "- all games\n" +
                        SET_TEXT_COLOR_BLUE + "   join <gameID> [WHITE|BLACK] " + SET_TEXT_COLOR_MAGENTA + "- to join a game\n" +
                        SET_TEXT_COLOR_BLUE + "   observe <gameID> " + SET_TEXT_COLOR_MAGENTA + "- a game\n" +
                        SET_TEXT_COLOR_BLUE + "   logout " + SET_TEXT_COLOR_MAGENTA + "- when you are done playing chess\n" +
                        SET_TEXT_COLOR_BLUE + "   quit " + SET_TEXT_COLOR_MAGENTA + "- to quit\n" +
                        SET_TEXT_COLOR_BLUE + "   help " + SET_TEXT_COLOR_MAGENTA + "- with possible commands\n" + RESET_ALL;
        System.out.println(authData == null ? preLoginHelp : postLoginHelp);
    }

    // PRE-LOGIN COMMANDS

    public void register(String username, String password, String email) throws ResponseException {
        try {
            URI uri = new URI("http://localhost:" + port + "/user");
            HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
            http.setRequestMethod("POST");

            // Specify that we are going to write out data
            http.setDoOutput(true);

            // Write out a header
            http.setRequestProperty("Content-Type", "application/json");

            // Write out the body
            RegisterRequest requestBody = new RegisterRequest(username, password, email);
            String jsonRequestBody = new Gson().toJson(requestBody);
            try (OutputStream outputStream = http.getOutputStream()) {
                outputStream.write(jsonRequestBody.getBytes());
            }

            // Read the response
            try (InputStream respBodyBytes = http.getInputStream()) {
                InputStreamReader inputStreamReader = new InputStreamReader(respBodyBytes);
                RegisterResponse responseBody = new Gson().fromJson(inputStreamReader, RegisterResponse.class);
                this.authData = new AuthData(responseBody.authToken(), responseBody.username());
            }
        } catch (IOException | URISyntaxException e) {
            throw new ResponseException("Registration failed. Error: " + e.getMessage());
        }
    }

    public void login(String username, String password) throws ResponseException {
        try {
            URI uri = new URI("http://localhost:" + port + "/session");
            HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
            http.setRequestMethod("POST");

            // Specify that we are going to write out data
            http.setDoOutput(true);

            // Write out a header
            http.setRequestProperty("Content-Type", "application/json");

            // Write out the body
            LoginRequest requestBody = new LoginRequest(username, password);
            String jsonRequestBody = new Gson().toJson(requestBody);
            try (OutputStream outputStream = http.getOutputStream()) {
                outputStream.write(jsonRequestBody.getBytes());
            }

            // Read the response
            try (InputStream respBodyBytes = http.getInputStream()) {
                InputStreamReader inputStreamReader = new InputStreamReader(respBodyBytes);
                LoginResponse responseBody = new Gson().fromJson(inputStreamReader, LoginResponse.class);
                this.authData = new AuthData(responseBody.authToken(), responseBody.username());
            }
        } catch (IOException | URISyntaxException e) {
            throw new ResponseException("Login failed. Error: " + e.getMessage());
        }
    }

    // POST-LOGIN COMMANDS

    public CreateGameResponse createGame(String authToken, String gameName) throws ResponseException {
        try {
            URI uri = new URI("http://localhost:" + port + "/game");
            HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
            http.setRequestMethod("POST");

            // Specify that we are going to write out data
            http.setDoOutput(true);

            // Write out a header
            http.setRequestProperty("Content-Type", "application/json");
            http.setRequestProperty("Authorization", authToken);

            // Write out the body
            String jsonRequestBody = new Gson().toJson(Map.of("gameName", gameName));
            try (OutputStream outputStream = http.getOutputStream()) {
                outputStream.write(jsonRequestBody.getBytes());
            }

            // Read the response
            try (InputStream respBodyBytes = http.getInputStream()) {
                InputStreamReader inputStreamReader = new InputStreamReader(respBodyBytes);
                return new Gson().fromJson(inputStreamReader, CreateGameResponse.class);
            }
        } catch (IOException | URISyntaxException e) {
            throw new ResponseException("Failed to create game. Error: " + e.getMessage());
        }
    }

    public ListGamesResponse listGames(String authToken) throws ResponseException {
        try {
            URI uri = new URI("http://localhost:" + port + "/game");
            HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
            http.setRequestMethod("GET");

            // Write out a header
            http.setRequestProperty("Content-Type", "application/json");
            http.setRequestProperty("Authorization", authToken);

            // Read the response
            try (InputStream respBodyBytes = http.getInputStream()) {
                InputStreamReader inputStreamReader = new InputStreamReader(respBodyBytes);
                return new Gson().fromJson(inputStreamReader, ListGamesResponse.class);
            }
        } catch (IOException | URISyntaxException e) {
            throw new ResponseException("Failed to list games. Error: " + e.getMessage());
        }
    }

    public void joinGame(String authToken, ChessGame.TeamColor clientColor, Integer gameID) throws ResponseException {
        try {
            URI uri = new URI("http://localhost:" + port + "/game");
            HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
            http.setRequestMethod("PUT");

            // Specify that we are going to write out data
            http.setDoOutput(true);

            // Write out a header
            http.setRequestProperty("Content-Type", "application/json");
            http.setRequestProperty("Authorization", authToken);

            // Write out the body
            JoinGameRequest requestBody = new JoinGameRequest(authToken, clientColor, gameID);
            String jsonRequestBody = new Gson().toJson(requestBody);
            try (OutputStream outputStream = http.getOutputStream()) {
                outputStream.write(jsonRequestBody.getBytes());
            }

            // Read the response (no response body)
            http.getInputStream();

        } catch (IOException | URISyntaxException e) {
            throw new ResponseException("Failed to join game. Error: " + e.getMessage());
        }
    }

    public void observeGame(String authToken, Integer gameID) throws ResponseException {
        try {
            URI uri = new URI("http://localhost:" + port + "/game");
            HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
            http.setRequestMethod("PUT");

            // Specify that we are going to write out data
            http.setDoOutput(true);

            // Write out a header
            http.setRequestProperty("Content-Type", "application/json");
            http.setRequestProperty("Authorization", authToken);

            // Write out the body (with no TeamColor to join as an observer)
            JoinGameRequest requestBody = new JoinGameRequest(authToken, null, gameID);
            String jsonRequestBody = new Gson().toJson(requestBody);
            try (OutputStream outputStream = http.getOutputStream()) {
                outputStream.write(jsonRequestBody.getBytes());
            }

            // Read the response (no response body)
            http.getInputStream();

        } catch (IOException | URISyntaxException e) {
            throw new ResponseException("Failed to join game. Error: " + e.getMessage());
        }
    }

    public void logout() throws ResponseException {
        try {
            URI uri = new URI("http://localhost:" + port + "/session");
            HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
            http.setRequestMethod("DELETE");

            // Specify that we are going to write out data
            http.setDoOutput(true);

            // Write out a header
            http.setRequestProperty("Content-Type", "application/json");
            http.setRequestProperty("Authorization", getAuthToken());

            // Read the response (no response body)
            http.getInputStream();

            // Clear client's auth data
            authData = null;
        } catch (IOException | URISyntaxException e) {
            throw new ResponseException("Failed to logout. Error: " + e.getMessage());
        }
    }

    // GAMEPLAY COMMANDS


    // HELPER METHODS

    private int extractPort(String url) {
        try {
            return new URI(url).getPort();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
