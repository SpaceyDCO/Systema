package com.tamv.systema.frontend;

import com.tamv.systema.frontend.API.ApiService;
import com.tamv.systema.frontend.model.Invoice;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class InvoiceViewController {
    private final ApiService api;
    @FXML
    public TableView<Invoice> invoiceTable;
    @FXML
    public TableColumn<Invoice, Long> idColumn;
    @FXML
    public TableColumn<Invoice, String> customerNameColumn;
    @FXML
    public TableColumn<Invoice, LocalDate> invoiceDateColumn;
    @FXML
    public TableColumn<Invoice, LocalDate> dueDateColumn;
    @FXML
    public TableColumn<Invoice, BigDecimal> totalAmountColumn;
    @FXML
    public TableColumn<Invoice, String> statusColumn;
    @FXML
    public Button newButton;
    @FXML
    public Button editButton;
    @FXML
    public Button deleteButton;
    public InvoiceViewController(ApiService api) {
        this.api = api;
    }
    @FXML
    public void initialize() {
        System.out.println("Invoice view has been loaded. Populating...");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        invoiceDateColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        totalAmountColumn.setCellFactory(column -> new TableCell<Invoice, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if(empty || amount == null) {
                    setText(null);
                }else {
                    setText(String.format("$ %.2f", amount));
                }
            }
        });
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusName"));
        new Thread(() -> {
            List<Invoice> invoices = api.getInvoices();
            ObservableList<Invoice> observableList = FXCollections.observableArrayList(invoices);
            Platform.runLater(() -> invoiceTable.setItems(observableList));
        }).start();
        this.editButton.setDisable(true);
        this.deleteButton.setDisable(true);
        this.invoiceTable.getSelectionModel().selectedItemProperty().addListener((observable, oldSelection, newSelection) -> {
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
    public void handleNewInvoice() {
        openInvoiceForm(null);
    }
    @FXML
    public void handleEditInvoice() {
        Invoice selectedInvoice = this.invoiceTable.getSelectionModel().getSelectedItem();
        if(selectedInvoice == null) return;
        openInvoiceForm(selectedInvoice);
    }
    @FXML
    public void handleDeleteInvoice() {
        Invoice selectedInvoice = this.invoiceTable.getSelectionModel().getSelectedItem();
        if (selectedInvoice == null) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Invoice");
        alert.setHeaderText("Are you sure you want to delete this invoice?");
        alert.setContentText("Invoice ID: " + selectedInvoice.getId() +
                "\nCustomer: " + selectedInvoice.getCustomerName() +
                "\nTotal: $" + selectedInvoice.getTotalAmount() +
                "\nThis action cannot be undone.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.out.println("User confirmed deletion for invoice " + selectedInvoice.getId());
            new Thread(() -> {
                boolean success = api.deleteInvoice(selectedInvoice.getId());
                Platform.runLater(() -> {
                    if (success) {
                        refreshTable();
                    } else {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Deletion Failed");
                        errorAlert.setHeaderText("Could not delete invoice");
                        errorAlert.setContentText("The invoice could not be deleted from the database. Please try again.");
                        errorAlert.showAndWait();
                    }
                });
            }).start();
        } else {
            System.out.println("User canceled deletion");
        }
    }
    private void openInvoiceForm(Invoice invoice) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tamv/systema/frontend/invoice-form.fxml"));
            Parent popup = loader.load();
            InvoiceFormController controller = loader.getController();
            controller.setApi(this.api);
            controller.setOnSaveSuccess(this::refreshTable);
            controller.setInvoiceData(invoice);
            Stage stage = new Stage();
            stage.setTitle(invoice == null ? "New invoice" : "Edit invoice");
            stage.setScene(new Scene(popup));
            stage.setResizable(false);
            stage.showAndWait();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void refreshTable() {
        new Thread(() -> {
            List<Invoice> invoices = api.getInvoices();
            ObservableList<Invoice> observableList = FXCollections.observableArrayList(invoices);
            Platform.runLater(() -> invoiceTable.setItems(observableList));
        }).start();
    }
}
