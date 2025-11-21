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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

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
            Platform.runLater(() -> {
                customerTable.setItems(observableList);
            });
        }).start();
    }
    @FXML
    public void handleNewCustomer(ActionEvent event) {
        System.out.println("New customer button clicked");
        openCustomerForm(null);
    }
    private void openCustomerForm(Customer customer) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/tamv/systema/frontend/customer-form.fxml"));
            Parent popup = fxmlLoader.load();
            CustomerFormController controller = fxmlLoader.getController();
            controller.setCustomerData(customer);
            controller.setApi(this.api);
            controller.setOnSaveSuccess(this::refreshTable);
            //TODO: detect if it's an edit of an existing customer, call the controller with fxmlLoader.getController() and set the data of the clicked customer...
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
            Platform.runLater(() -> {
                customerTable.setItems(observableList);
            });
        }).start();
    }
}
