package com.tamv.systema.frontend;

import com.tamv.systema.frontend.API.ApiService;
import com.tamv.systema.frontend.model.Customer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import lombok.Setter;

import java.io.IOException;
import java.util.Optional;

public class CustomerFormController {
    @FXML
    public TextArea addressField;
    @FXML
    public Label titleLabel;
    @FXML
    public Button deleteButton;
    @FXML
    private Label errorLabel;
    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    private final ApiService api;
    private Customer customer;
    @Setter
    private StackPane contentArea;
    public CustomerFormController(ApiService api) {
        this.api = api;
    }
    @FXML
    public void onBack() {
        goBack();
    }
    @FXML
    //TODO: Let users add customer's address (optional)
    public void onSave() {
        if(!validateFields()) return;
        String name = this.nameField.getText().trim();
        String email = this.emailField.getText().trim();
        String phoneNumber = this.phoneField.getText().trim();
        new Thread(() -> {
            try {
                boolean success;
                if(customer == null || customer.getId() == null) {
                    Customer newCustomer = new Customer();
                    newCustomer.setFullName(name);
                    newCustomer.setEmail(email);
                    newCustomer.setPhoneNumber(phoneNumber);
                    success = api.createCustomer(newCustomer) != null;
                }else {
                    customer.setFullName(name);
                    customer.setEmail(email);
                    customer.setPhoneNumber(phoneNumber);
                    success = api.updateCustomer(customer.getId(), customer);
                }
                Platform.runLater(() -> {
                    if(success) goBack();
                    else showError("Failed to save customer. Please try again.");
                });
            }catch (Exception e) {
                Platform.runLater(() -> showError("An unexpected error happened while trying to save the customer. Please contact an administrator."));
            }
        }).start();
    }
    public void setCustomer(Customer customer) {
        this.customer = customer;
        if(customer == null || customer.getId() == null) {
            this.titleLabel.setText("New Customer");
            this.deleteButton.setVisible(false);
            this.deleteButton.setManaged(false);
        }else {
            this.titleLabel.setText("Edit Customer");
            populateFields();
        }
    }
    @FXML
    public void onDelete() {
        if(customer == null || customer.getId() == null) return; //Fail-safe
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Customer");
        alert.setHeaderText("Are you sure you want to delete this customer?");
        alert.setContentText("Customer: " + customer.getFullName() + "\nThis action cannot be undone.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                boolean success = api.deleteCustomer(customer.getId());
                Platform.runLater(() -> {
                    if (success) goBack();
                    else showError("Failed to delete customer. Please try again.");
                });
            }).start();
        }
    }
    private void populateFields() {
        this.nameField.setText(customer.getFullName());
        this.phoneField.setText(customer.getPhoneNumber());
        this.emailField.setText(customer.getEmail());
    }
    private boolean validateFields() {
        String name = this.nameField.getText().trim();
        String email = this.emailField.getText().trim();
        String phoneNumber = this.phoneField.getText().trim();
        if (name.isEmpty()) {
            showError("Name is required");
            nameField.requestFocus();
            return false;
        }
        if (email.isEmpty()) {
            showError("Email is required");
            emailField.requestFocus();
            return false;
        }
        if (!email.contains("@")) {
            showError("Please enter a valid email address");
            emailField.requestFocus();
            return false;
        }
        if (phoneNumber.isEmpty()) {
            showError("Phone number is required");
            phoneField.requestFocus();
            return false;
        }
        this.errorLabel.setVisible(false);
        this.errorLabel.setManaged(false);
        return true;
    }
    private void showError(String error) {
        this.errorLabel.setText(error);
        this.errorLabel.setVisible(true);
        this.errorLabel.setManaged(true);

    }
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tamv/systema/frontend/customer-view.fxml"));
            loader.setControllerFactory(controllerClass -> new CustomerViewController(this.api, this.contentArea));
            Parent customerView = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(customerView);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
