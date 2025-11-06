package com.tamv.systema.frontend.API;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class AuthService {
    private static final String API_BASE_URL = "http://localhost:8080/api/v1";
    private final HttpClient client = HttpClient.newHttpClient();
    // TODO: Refactor login to use a dedicated token-based endpoint.
    public boolean login(String username, String password) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/customers"))
                .header("Authorization", createBasicAuthHeader(username, password))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private String createBasicAuthHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }
}
