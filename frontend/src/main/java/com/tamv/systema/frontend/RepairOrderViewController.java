package com.tamv.systema.frontend;

import com.tamv.systema.frontend.API.ApiService;
import com.tamv.systema.frontend.model.RepairOrder;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public class RepairOrderViewController {
    private final ApiService api;
    @FXML
    public TableView<RepairOrder> repairOrderTable;
    @FXML
    public TableColumn<RepairOrder, Long> idColumn;
    @FXML
    public TableColumn<RepairOrder, String> customerNameColumn;
    @FXML
    public TableColumn<RepairOrder, String> equipmentColumn;
    @FXML
    public TableColumn<RepairOrder, String> serialColumn;
    @FXML
    public TableColumn<RepairOrder, LocalDate> dateColumn;
    @FXML
    public TableColumn<RepairOrder, String> statusColumn;
    @FXML
    public Button newButton;
    @FXML
    public Button editButton;
    @FXML
    public Button deleteButton;
    public RepairOrderViewController(ApiService api) {
        this.api = api;
    }
    @FXML
    public void initialize() {
        System.out.println("Populating repair order table...");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        equipmentColumn.setCellValueFactory(new PropertyValueFactory<>("equipmentName"));
        serialColumn.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateReceived"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusName"));
        new Thread(() -> {
            List<RepairOrder> orders = api.getRepairOrders();
            ObservableList<RepairOrder> observableList = FXCollections.observableArrayList(orders);
            Platform.runLater(() -> this.repairOrderTable.setItems(observableList));
        }).start();
        this.editButton.setDisable(true);
        this.deleteButton.setDisable(true);
        this.repairOrderTable.getSelectionModel().selectedItemProperty().addListener((observable, oldSelection, newSelection) -> {
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
    public void handleNewOrder(ActionEvent event) {
        openRepairOrderForm(null);
    }
    @FXML
    public void handleDelete(ActionEvent event) {
        RepairOrder selectedOrder = this.repairOrderTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Repair Order");
        alert.setHeaderText("Are you sure you want to delete this repair order?");
        alert.setContentText("Order ID: " + selectedOrder.getId() +
                "\nEquipment: " + selectedOrder.getEquipmentName() +
                "\nThis action cannot be undone.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.out.println("User confirmed deletion for repair order " + selectedOrder.getId());
            new Thread(() -> {
                boolean success = api.deleteRepairOrder(selectedOrder.getId());
                Platform.runLater(() -> {
                    if (success) {
                        refreshTable();
                    } else {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Deletion Failed");
                        errorAlert.setHeaderText("Could not delete repair order");
                        errorAlert.setContentText("The repair order could not be deleted from the database. Please try again.");
                        errorAlert.showAndWait();
                    }
                });
            }).start();
        } else {
            System.out.println("User canceled deletion");
        }
    }
    @FXML
    public void handleEdit(ActionEvent event) {
        RepairOrder selectedOrder = repairOrderTable.getSelectionModel().getSelectedItem();
        if(selectedOrder == null) return;
        openRepairOrderForm(selectedOrder);
    }
    private void openRepairOrderForm(RepairOrder order) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/tamv/systema/frontend/repair-order-form.fxml"));
            Parent popup = fxmlLoader.load();
            RepairOrderFormController controller = fxmlLoader.getController();
            controller.setRepairOrderData(order);
            controller.setApi(this.api);
            controller.setOnSaveSuccess(this::refreshTable);
            Stage stage = new Stage();
            stage.setTitle(order == null ? "New Order" : "Edit Order");
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
            List<RepairOrder> orders = api.getRepairOrders();
            ObservableList<RepairOrder> observableList = FXCollections.observableArrayList(orders);
            Platform.runLater(() -> this.repairOrderTable.setItems(observableList));
        }).start();
    }
}
