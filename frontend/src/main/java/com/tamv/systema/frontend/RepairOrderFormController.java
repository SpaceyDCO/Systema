package com.tamv.systema.frontend;

import com.tamv.systema.frontend.API.ApiService;
import com.tamv.systema.frontend.model.Customer;
import com.tamv.systema.frontend.model.RepairOrder;
import com.tamv.systema.frontend.model.Status;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;

import java.util.List;

public class RepairOrderFormController {
    @FXML
    public ComboBox<Customer> customerComboBox;
    @FXML
    public TextField equipmentNameField;
    @FXML
    public TextField serialNumberField;
    @FXML
    public TextArea reportedIssueField;
    @FXML
    public ComboBox<Status> statusComboBox;
    @FXML
    public TextArea technicianNotesField;
    @FXML
    public Button saveButton;
    @FXML
    public Button cancelButton;
    @FXML
    public Label errorLabel;
    @Setter
    private ApiService api;
    @Setter
    private Runnable onSaveSuccess;
    private RepairOrder currentOrder;
    @FXML
    public void handleSave(ActionEvent event) {
        Customer selectedCustomer = this.customerComboBox.getValue();
        String equipmentName = this.equipmentNameField.getText();
        String serialNumber = this.serialNumberField.getText();
        String reportedIssue = this.reportedIssueField.getText();
        Status selectedStatus = this.statusComboBox.getValue();
        String technicianNotes = this.technicianNotesField.getText();
        if (selectedCustomer == null) {
            errorLabel.setText("Please select a customer.");
            return;
        }
        if (equipmentName == null || equipmentName.trim().isEmpty()) {
            errorLabel.setText("Equipment name is required.");
            return;
        }
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            errorLabel.setText("Serial number is required.");
            return;
        }
        if (reportedIssue == null || reportedIssue.trim().isEmpty()) {
            errorLabel.setText("Reported issue is required.");
            return;
        }
        errorLabel.setText("");
        final boolean isEditing = this.currentOrder != null && this.currentOrder.getId() != null;
        this.saveButton.setDisable(true);
        this.cancelButton.setDisable(true);
        if(isEditing) {
            new Thread(() -> {
                boolean success = true;
                if(selectedStatus != null && !selectedStatus.getId().equals(currentOrder.getStatus().getId())) {
                    success = api.updateRepairOrderStatus(currentOrder.getId(), selectedStatus.getId());
                }
                if(success && technicianNotes != null && !technicianNotes.equals(currentOrder.getTechnicianNotes())) {
                    success = api.updateRepairOrderNotes(currentOrder.getId(), technicianNotes);
                }
                final boolean finalSuccess = success;
                Platform.runLater(() -> {
                    if(finalSuccess) {
                        if(this.onSaveSuccess != null) onSaveSuccess.run();
                        Stage stage = (Stage) this.saveButton.getScene().getWindow();
                        stage.close();
                    }else {
                        this.errorLabel.setText("Failed to update repair order. Please try again.");
                        this.saveButton.setDisable(false);
                        this.cancelButton.setDisable(false);
                    }
                });
            }).start();
        }else {
            new Thread(() -> {
                RepairOrder savedOrder = api.createRepairOrder(selectedCustomer.getId(), equipmentName, serialNumber, reportedIssue);
                Platform.runLater(() -> {
                    if(savedOrder != null) {
                        if(this.onSaveSuccess != null) this.onSaveSuccess.run();
                        Stage stage = (Stage) this.saveButton.getScene().getWindow();
                        stage.close();
                    }else {
                        this.errorLabel.setText("Failed to create repair order. Please try again.");
                        this.saveButton.setDisable(false);
                        this.cancelButton.setDisable(false);
                    }
                });
            }).start();
        }
    }
    @FXML
    public void handleCancel(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }
    public void setRepairOrderData(RepairOrder order) {
        this.currentOrder = order;
        loadCustomers();
        loadStatuses();
        final boolean isEditing = order != null && order.getId() != null;
        if(isEditing) {
            this.customerComboBox.setValue(order.getCustomer());
            this.customerComboBox.setDisable(true);
            this.equipmentNameField.setText(order.getEquipmentName());
            this.equipmentNameField.setDisable(true);
            this.serialNumberField.setText(order.getSerialNumber());
            this.serialNumberField.setDisable(true);
            this.reportedIssueField.setText(order.getReportedIssue());
            this.reportedIssueField.setDisable(true);
            this.statusComboBox.setValue(order.getStatus());
            this.technicianNotesField.setText(order.getTechnicianNotes());
        }else {
            this.customerComboBox.setDisable(false);
            this.equipmentNameField.setDisable(false);
            this.serialNumberField.setDisable(false);
            this.reportedIssueField.setDisable(false);
            this.statusComboBox.setDisable(true);
            this.technicianNotesField.setDisable(true);
        }
    }
    private void loadCustomers() {
        new Thread(() -> {
            List<Customer> customers = api.getCustomers();
            ObservableList<Customer> observableList = FXCollections.observableArrayList(customers);
            Platform.runLater(() -> this.customerComboBox.setItems(observableList));
        }).start();
    }
    private void loadStatuses() {
        new Thread(() -> {
            List<Status> statuses = api.getStatuses();
            List<Status> repairStatuses = statuses.stream()
                    .filter(s -> "REPAIR_ORDER".equals(s.getType()))
                    .toList();
            ObservableList<Status> observableList = FXCollections.observableArrayList(repairStatuses);
            Platform.runLater(() -> this.statusComboBox.setItems(observableList));
        }).start();
    }
}
