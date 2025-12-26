package com.tamv.systema.frontend;

import com.tamv.systema.frontend.API.ApiService;
import com.tamv.systema.frontend.Utils.General;
import com.tamv.systema.frontend.model.Customer;
import com.tamv.systema.frontend.model.RepairOrder;
import com.tamv.systema.frontend.model.Status;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import lombok.Setter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class RepairOrderFormController {
    @FXML
    public ComboBox<Status> statusComboBox;
    @FXML
    public ComboBox<Customer> customerComboBox;
    @FXML
    public Label titleLabel;
    @FXML
    public Button deleteButton;
    @FXML
    public TextField equipmentField;
    @FXML
    public TextField serialNumberField;
    @FXML
    public TextArea issueField;
    @FXML
    public TextArea notesField;
    @FXML
    public Label errorLabel;
    private RepairOrder order;
    private final ApiService api;
    @Setter
    private StackPane contentArea;
    private List<Customer> allCustomers;
    public RepairOrderFormController(ApiService api) {
        this.api = api;
    }
    @FXML
    public void initialize() {
        loadStatuses();
        loadCustomers();
    }
    @FXML
    public void onBack() {
        goBack();
    }
    @FXML
    public void onSave() {
        if(!validateFields()) return;
        Customer customer = this.customerComboBox.getValue();
        String equipmentDescription = this.equipmentField.getText();
        String serialNumber = this.serialNumberField.getText();
        String issueDescription = this.issueField.getText();
        Status status = this.statusComboBox.getValue();
        String technicianNotes = this.notesField.getText();
        new Thread(() -> {
            try {
                boolean success;
                if(order == null || order.getId() == null)
                    success = api.createRepairOrder(customer.getId(), equipmentDescription, serialNumber, issueDescription) != null;
                else success = api.updateRepairOrderStatusAndNotes(order.getId(), status.getId(), technicianNotes);
                Platform.runLater(() -> {
                    if(success) goBack();
                    else General.showError(this.errorLabel, "Failed to save repairOrder, please try again or contact an admin.");
                });
            }catch (Exception e) {
                General.showError(this.errorLabel, "There has been an error while trying to save the Repair Order. Please contact an admin.");
                e.printStackTrace();
            }
        }).start();
    }
    @FXML
    public void onDelete() {
        if(order == null || order.getId() == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Repair Order");
        alert.setHeaderText("Are you sure you want to delete this repair order?");
        alert.setContentText("Order for: " + order.getCustomerName() + "\nID: " + order.getId() + "\nThis action cannot be undone.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                boolean success = api.deleteRepairOrder(order.getId());
                Platform.runLater(() -> {
                    if (success) goBack();
                    else General.showError(this.errorLabel, "Failed to delete invoice. Please try again.");
                });
            }).start();
        }
    }
    protected void setRepairOrder(RepairOrder order) {
        this.order = order;
        if(order == null) {
            this.titleLabel.setText("New Repair Order");
            this.deleteButton.setVisible(false);
            this.deleteButton.setManaged(false);
            this.statusComboBox.setDisable(true);
        }else {
            this.titleLabel.setText("Edit Repair Order #" + this.order.getId());
            populateFields();
            this.equipmentField.setDisable(true);
            this.serialNumberField.setDisable(true);
            this.issueField.setDisable(true);
            this.customerComboBox.setDisable(true);
        }
    }
    private boolean validateFields() {
        Customer customer = this.customerComboBox.getValue();
        String equipmentDescription = this.equipmentField.getText();
        String issueDescription = this.issueField.getText();
        if(customer == null) {
            General.showError(this.errorLabel, "Customer is required.");
            return false;
        }
        if(equipmentDescription.trim().isEmpty()) {
            General.showError(this.errorLabel, "Equipment description is required.");
            return false;
        }
        if(issueDescription.trim().isEmpty()) {
            General.showError(this.errorLabel, "Issue description is required.");
            return false;
        }
        this.errorLabel.setText("");
        this.errorLabel.setVisible(false);
        this.errorLabel.setManaged(false);
        return true;
    }
    private void populateFields() {
        this.equipmentField.setText(this.order.getEquipmentName());
        this.serialNumberField.setText(this.order.getSerialNumber());
        this.issueField.setText(this.order.getReportedIssue());
        if(this.order.getTechnicianNotes() != null && !this.order.getTechnicianNotes().isEmpty()) this.notesField.setText(this.order.getTechnicianNotes());
        this.statusComboBox.setValue(this.order.getStatus());
    }
    private void loadCustomers() {
        new Thread(() -> {
            allCustomers = api.getCustomers();
            ObservableList<Customer> observableList = FXCollections.observableArrayList(allCustomers);
            Platform.runLater(() -> {
                this.customerComboBox.setItems(observableList);
                if(order != null && order.getCustomer() != null) this.customerComboBox.setValue(order.getCustomer());
            });
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
//    private void loadStatuses() {
//        General.loadEntities(api::getStatuses, entityList -> {
//            ObservableList<Status> observableList = FXCollections.observableArrayList(entityList);
//            this.statusComboBox.setItems(observableList);
//        });
//    }
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tamv/systema/frontend/repair-order-view.fxml"));
            loader.setControllerFactory(controllerClass -> new RepairOrderViewController(this.api, this.contentArea));
            Parent repairView = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(repairView);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
