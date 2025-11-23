package com.tamv.systema.frontend.API;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tamv.systema.frontend.model.Customer;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ApiService {
    private String username;
    private String password;
    private static final String API_BASE_URL = "http://localhost:8080/api/v1";
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    // TODO: Refactor login to use a dedicated token-based endpoint.
    public boolean login(String username, String password) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/customers"))
                .header("Authorization", createBasicAuthHeader(username, password))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            this.username = username;
            this.password = password;
            return response.statusCode() == 200;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<Customer> getCustomers() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/customers"))
                .header("Authorization", createBasicAuthHeader(this.username, this.password))
                .GET()
                .build();
        try {
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200) {
                Gson gson = new Gson();
                Type customerListType = new TypeToken<ArrayList<Customer>>(){}.getType();
                return gson.fromJson(response.body(), customerListType);
            }else {
                System.out.println("Failed to fetch customers. Status: " + response.statusCode());
                //TODO: something to display the failure in the application, apart from the console
                return new ArrayList<>();
            }
        }catch(Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    public Customer createCustomer(Customer customer) {
        String jsonBody = gson.toJson(customer);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/customers"))
                .header("Authorization", createBasicAuthHeader(this.username, this.password))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        try {
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 201) {
                return gson.fromJson(response.body(), Customer.class);
            }else {
                System.err.println("Failed to create customer, status: " + response.statusCode() + ", Body: " + response.body());
                return null;
            }
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public boolean deleteCustomer(Long customerId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/customers/" + customerId))
                .header("Authorization", createBasicAuthHeader(this.username, this.password))
                .DELETE()
                .build();
        try {
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 204;
        }catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private String createBasicAuthHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }
}
