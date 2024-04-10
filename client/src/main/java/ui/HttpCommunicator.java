package ui;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

public class HttpCommunicator {
    private final Integer port;
    
    public HttpCommunicator(Integer port) {
        this.port = port;
    }
    
    public <T> T sendPostRequest(String path, String authToken, Object requestBody, Class<T> responseType) throws IOException, URISyntaxException {
        URI uri = new URI("http://localhost:" + port + path);
        HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setRequestProperty("Content-Type", "application/json");
        if (authToken != null) {
            http.setRequestProperty("Authorization", authToken);
        }
        
        String jsonRequestBody = new Gson().toJson(requestBody);
        try (OutputStream outputStream = http.getOutputStream()) {
            outputStream.write(jsonRequestBody.getBytes());
        }
        
        try (InputStream respBodyBytes = http.getInputStream()) {
            InputStreamReader inputStreamReader = new InputStreamReader(respBodyBytes);
            return new Gson().fromJson(inputStreamReader, responseType);
        }
    }
    
    public <T> T sendGetRequest(String path, String authToken, Class<T> responseType) throws IOException, URISyntaxException {
        URI uri = new URI("http://localhost:" + port + path);
        HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
        http.setRequestMethod("GET");
        http.setRequestProperty("Content-Type", "application/json");
        http.setRequestProperty("Authorization", authToken);
        
        try (InputStream respBodyBytes = http.getInputStream()) {
            InputStreamReader inputStreamReader = new InputStreamReader(respBodyBytes);
            return new Gson().fromJson(inputStreamReader, responseType);
        }
    }
    
    public void sendPutRequest(String path, String authToken, Object requestBody) throws IOException, URISyntaxException {
        URI uri = new URI("http://localhost:" + port + path);
        HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
        http.setRequestMethod("PUT");
        http.setDoOutput(true);
        http.setRequestProperty("Content-Type", "application/json");
        http.setRequestProperty("Authorization", authToken);
        
        String jsonRequestBody = new Gson().toJson(requestBody);
        try (OutputStream outputStream = http.getOutputStream()) {
            outputStream.write(jsonRequestBody.getBytes());
        }
        
        http.getInputStream();
    }
    
    public void sendDeleteRequest(String path, String authToken) throws IOException, URISyntaxException {
        URI uri = new URI("http://localhost:" + port + path);
        HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
        http.setRequestMethod("DELETE");
        http.setDoOutput(true);
        http.setRequestProperty("Content-Type", "application/json");
        http.setRequestProperty("Authorization", authToken);
        
        http.getInputStream();
    }
}