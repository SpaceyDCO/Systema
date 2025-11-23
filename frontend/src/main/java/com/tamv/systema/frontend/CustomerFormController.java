package com.tamv.systema.frontend;

import com.tamv.systema.frontend.API.ApiService;
import com.tamv.systema.frontend.model.Customer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Setter;

public class CustomerFormController {
    @FXML
    private Label errorLabel;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    private Customer currentCustomer;
    @Setter
    private ApiService api;
    @Setter
    private Runnable onSaveSuccess;
    @FXML
    public void handleSave(ActionEvent event) {
        String fullName = this.nameField.getText();
        String email = this.emailField.getText();
        String phone = this.phoneField.getText();
        if (fullName == null || fullName.trim().isEmpty()) {
            errorLabel.setText("Full Name is required.");
            return;
        }
        if (email == null || email.trim().isEmpty()) {
            errorLabel.setText("Email is required.");
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            errorLabel.setText("Invalid email format.");
            return;
        }
        if (phone == null || phone.trim().isEmpty()) {
            errorLabel.setText("Phone Number is required.");
            return;
        }
        errorLabel.setText("");
        final boolean isEditing = this.currentCustomer != null && this.currentCustomer.getId() != null;
        if(currentCustomer == null) {
            currentCustomer = new Customer();
        }
        this.currentCustomer.setFullName(fullName);
        this.currentCustomer.setEmail(email);
        this.currentCustomer.setPhoneNumber(phone);
        this.saveButton.setDisable(true);
        this.cancelButton.setDisable(true);
        new Thread(() -> {
            Customer savedCustomer = isEditing ? api.updateCustomer(this.currentCustomer.getId(), this.currentCustomer) : api.createCustomer(currentCustomer);
            Platform.runLater(() -> {
                if(savedCustomer != null) {
                    if(this.onSaveSuccess != null) {
                        this.onSaveSuccess.run();
                    }
                    Stage stage = (Stage) this.saveButton.getScene().getWindow();
                    stage.close();
                }else {
                    this.errorLabel.setText("Failed to save customer, please try again...");
                    this.saveButton.setDisable(false);
                    this.cancelButton.setDisable(false);
                }
            });
        }).start();
        System.out.println("Saving customer: " + fullName);
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
    @FXML
    public void handleCancel(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }
    public void setCustomerData(Customer customer) {
        this.currentCustomer = customer;
        if(customer != null) {
            this.nameField.setText(customer.getFullName());
            this.emailField.setText(customer.getEmail());
            this.phoneField.setText(customer.getPhoneNumber());
        }
    }
}
