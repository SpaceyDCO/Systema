package com.tamv.systema.frontend;

import com.tamv.systema.frontend.API.ApiService;
import com.tamv.systema.frontend.model.Customer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class CustomerViewController {
    private final ApiService api;
    @FXML
    public TableView<Customer> customerTable;
    @FXML
    public TableColumn<Customer, Long> idColumn;
    @FXML
    public TableColumn<Customer, String> nameColumn;
    @FXML
    public TableColumn<Customer, String> emailColumn;
    @FXML
    public TableColumn<Customer, String> phoneColumn;
    @FXML
    public Button editButton;
    @FXML
    public Button deleteButton;

    public CustomerViewController(ApiService api) {
        this.api = api;
    }
    @FXML
    public void initialize() {
        System.out.println("Customer view has been loaded... Populating table");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        new Thread(() -> {
            List<Customer> customers = api.getCustomers();
            ObservableList<Customer> observableList = FXCollections.observableArrayList(customers);
            Platform.runLater(() -> customerTable.setItems(observableList));
        }).start();
        this.editButton.setDisable(true);
        this.deleteButton.setDisable(true);
        this.customerTable.getSelectionModel().selectedItemProperty().addListener((observable, oldSelection, newSelection) -> {
            if(newSelection != null) {
                this.editButton.setDisable(false);
                this.deleteButton.setDisable(false);
            }else {
                this.editButton.setDisable(true);
                this.deleteButton.setDisable(true);
            }
        });
    }
    @FXML
    public void handleNewCustomer(ActionEvent event) {
        openCustomerForm(null);
    }
    @FXML
    public void handleDeleteCustomer(ActionEvent event) {
        Customer selectedCustomer = this.customerTable.getSelectionModel().getSelectedItem();
        if(selectedCustomer == null) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Customer");
        alert.setHeaderText("Are you sure you want to delete this customer?");
        alert.setContentText("Customer: " + selectedCustomer.getFullName() + "\nThis action cannot be undone.");
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            System.out.println("User confirmed deletion for customer " + selectedCustomer.getFullName() + " with id " + selectedCustomer.getId());
            new Thread(() -> {
                boolean success = api.deleteCustomer(selectedCustomer.getId());
                Platform.runLater(() -> {
                    if(success) {
                        refreshTable();
                    }else {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Deletion Failed");
                        errorAlert.setHeaderText("Could not delete customer");
                        errorAlert.setContentText("The customer could not be deleted from the database. Please try again.");
                        errorAlert.showAndWait();
                    }
                });
            }).start();
        }else {
            System.out.println("User canceled deletion");
        }
    }
    @FXML
    public void handleEditCustomer(ActionEvent event) {
        Customer selectedCustomer = this.customerTable.getSelectionModel().getSelectedItem();
        if(selectedCustomer == null) return;
        openCustomerForm(selectedCustomer);
    }
    private void openCustomerForm(Customer customer) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/tamv/systema/frontend/customer-form.fxml"));
            Parent popup = fxmlLoader.load();
            CustomerFormController controller = fxmlLoader.getController();
            controller.setCustomerData(customer);
            controller.setApi(this.api);
            controller.setOnSaveSuccess(this::refreshTable);
            Stage stage = new Stage();
            stage.setTitle(customer == null ? "New Customer" : "Edit Customer");
            stage.setScene(new Scene(popup));
            stage.setAlwaysOnTop(true);
            stage.setResizable(false);
            stage.showAndWait();
        }catch(IOException e) {
            e.printStackTrace();
        }
    }
    private void refreshTable() {
        new Thread(() -> {
            List<Customer> customers = api.getCustomers();
            ObservableList<Customer> observableList = FXCollections.observableArrayList(customers);
            Platform.runLater(() -> customerTable.setItems(observableList));
        }).start();
    }
}
