package com.tamv.systema.frontend.API;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.tamv.systema.frontend.model.*;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ApiService {
    private String username;
    private String password;
    private static final String API_BASE_URL = "http://localhost:8080/api/v1";
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, type, context) -> LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE))
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (date, type, context) -> new JsonPrimitive(date.format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .create();
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
    public boolean updateCustomer(Long id, Customer customer) {
        String jsonBody = gson.toJson(customer);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/customers/" + id))
                .header("Authorization", createBasicAuthHeader(this.username, this.password))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        try {
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        }catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<Product> getProducts() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/products"))
                .header("Authorization", createBasicAuthHeader(this.username, this.password))
                .GET()
                .build();
        try {
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200) {
                Type productListType = new TypeToken<ArrayList<Product>>(){}.getType();
                return gson.fromJson(response.body(), productListType);
            }else {
                System.out.println("Error while trying to fetch product list, status: " + response.statusCode());
                return new ArrayList<>();
            }
        }catch(Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    public Product createProduct(Product product) {
        String jsonBody = gson.toJson(product);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/products"))
                .header("Authorization", createBasicAuthHeader(this.username, this.password))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        try {
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 201) {
                return gson.fromJson(response.body(), Product.class);
            }else {
                System.err.println("Failed to create product, status: " + response.statusCode() + ", Body: " + response.body());
                return null;
            }
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public boolean deleteProduct(Long productId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/products/" + productId))
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
    public Product updateProduct(Long id, Product product) {
        String jsonBody = gson.toJson(product);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/products/" + id))
                .header("Authorization", createBasicAuthHeader(this.username, this.password))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        try {
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200) {
                return gson.fromJson(response.body(), Product.class);
            }else {
                System.err.println("Failed to update product. Status: " + response.statusCode());
                return null;
            }
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<Status> getStatuses() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/statuses"))
                .header("Authorization", createBasicAuthHeader(this.username, this.password))
                .GET()
                .build();
        try {
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200) {
                Type statusListType = new TypeToken<ArrayList<Status>>(){}.getType();
                return gson.fromJson(response.body(), statusListType);
            }else {
                System.err.println("Could not fetch status list, code: " + response.statusCode());
                return new ArrayList<>();
            }
        }catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    public List<RepairOrder> getRepairOrders() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/orders"))
                .header("Authorization", createBasicAuthHeader(this.username, this.password))
                .GET()
                .build();
        try {
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200) {
                Type repairListType = new TypeToken<ArrayList<RepairOrder>>(){}.getType();
                return gson.fromJson(response.body(), repairListType);
            }else {
                System.err.println("Could not fetch repair orders list, code: " + response.statusCode());
                return new ArrayList<>();
            }
        }catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    //TODO: endpoint to update a repair order's received date
    public RepairOrder createRepairOrder(Long customerId, String equipmentName, String serialNumber, String reportedIssue) {
        String jsonBody = String.format(
                "{\"customerId\":%d,\"equipmentName\":\"%s\",\"serialNumber\":\"%s\",\"reportedIssue\":\"%s\"}",
                customerId, equipmentName, serialNumber, reportedIssue
        );
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/orders"))
                .header("Authorization", createBasicAuthHeader(this.username, this.password))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        try {
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 201) {
                return gson.fromJson(response.body(), RepairOrder.class);
            }else {
                System.err.println("Failed to create repair order, status: " + response.statusCode());
                return null;
            }
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public boolean updateRepairOrderStatus(Long orderId, Long statusId) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("statusId", String.valueOf(statusId));
        String jsonBody = gson.toJson(requestBody);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/orders/" + orderId + "/status"))
                .header("Authorization", createBasicAuthHeader(this.username, this.password))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        try {
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean updateRepairOrderNotes(Long orderId, String technicianNotes) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("technicianNotes", technicianNotes);
        String jsonBody = gson.toJson(requestBody);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/orders/" + orderId + "/notes"))
                .header("Authorization", createBasicAuthHeader(this.username, this.password))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        try {
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean deleteRepairOrder(Long orderId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/orders/" + orderId))
                .header("Authorization", createBasicAuthHeader(this.username, this.password))
                .DELETE()
                .build();
        try {
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 204;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<Invoice> getInvoices() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/invoices"))
                .header("Authorization", createBasicAuthHeader(this.username, this.password))
                .GET()
                .build();
        try {
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200) {
                Type invoiceListType = new TypeToken<ArrayList<Invoice>>(){}.getType();
                return gson.fromJson(response.body(), invoiceListType);
            }else {
                System.err.println("Could not fetch invoices. Status: " + response.statusCode());
                return new ArrayList<>();
            }
        }catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    public Invoice createInvoice(Long customerId, List<InvoiceItemRequest> items, LocalDate dueDate) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("customerId", customerId);
        requestBody.put("items", items);
        requestBody.put("dueDate", dueDate);
        String jsonBody = gson.toJson(requestBody);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/invoices"))
                .header("Authorization", createBasicAuthHeader(this.username, this.password))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        try {
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 201) {
                return gson.fromJson(response.body(), Invoice.class);
            }else {
                System.err.println("Failed to create the invoice. Status: " + response.statusCode());
                return null;
            }
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public boolean updateInvoiceStatus(Long invoiceId, Long statusId) {
        Map<String, Long> requestBody = new HashMap<>();
        requestBody.put("statusId", statusId);
        String jsonBody = gson.toJson(requestBody);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/invoices/" + invoiceId + "/status"))
                .header("Authorization", createBasicAuthHeader(this.username, this.password))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        try {
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean deleteInvoice(Long invoiceId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/invoices/" + invoiceId))
                .header("Authorization", createBasicAuthHeader(this.username, this.password))
                .DELETE()
                .build();
        try {
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 204;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<InvoiceItem> getInvoiceItems(Long invoiceId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/invoices/" + invoiceId + "/items"))
                .header("Authorization", createBasicAuthHeader(this.username, this.password))
                .GET()
                .build();
        try {
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200) {
                Type itemListType = new TypeToken<ArrayList<InvoiceItem>>(){}.getType();
                return gson.fromJson(response.body(), itemListType);
            }else {
                System.err.println("Could not fetch invoice items. Status: " + response.statusCode());
                return new ArrayList<>();
            }
        }catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    private String createBasicAuthHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }
}
