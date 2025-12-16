package com.tamv.systema.frontend;

import com.tamv.systema.frontend.API.ApiService;
import com.tamv.systema.frontend.model.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InvoiceFormController {
    @FXML
    private ComboBox<Customer> customerComboBox;
    @FXML
    private DatePicker dueDatePicker;
    @FXML
    private TableView<InvoiceLineItem> itemsTable;
    @FXML
    private TableColumn<InvoiceLineItem, Product> productColumn;
    @FXML
    private TableColumn<InvoiceLineItem, Integer> quantityColumn;
    @FXML
    private TableColumn<InvoiceLineItem, BigDecimal> priceColumn;
    @FXML
    private TableColumn<InvoiceLineItem, BigDecimal> totalColumn;
    @FXML
    private Button addItemButton;
    @FXML
    private Button removeItemButton;
    @FXML
    private Label totalLabel;
    @FXML
    private GridPane statusGrid;
    @FXML
    private ComboBox<Status> statusComboBox;
    @FXML
    private Label errorLabel;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    private Invoice currentInvoice;
    private ObservableList<InvoiceLineItem> lineItems;
    private List<Product> availableProducts;
    @Setter
    private ApiService api;
    @Setter
    private Runnable onSaveSuccess;
    @FXML
    public void initialize() {
        lineItems = FXCollections.observableArrayList();
        itemsTable.setItems(lineItems);
        productColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().product()));
        this.quantityColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().quantity()));
        this.priceColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().price()));
        this.priceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) setText(null);
                else setText(String.format("$ %.2f", price));
            }
        });
        this.totalColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getLineTotal()));
        this.totalColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) setText(null);
                else setText(String.format("$ %.2f", total));
            }
        });
        lineItems.addListener((ListChangeListener.Change<? extends InvoiceLineItem> c) -> {
            updateTotalLabel();
        });
        this.removeItemButton.setDisable(true);
        this.itemsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldSelection, newSelection) -> {
            this.removeItemButton.setDisable(newSelection == null);
        });
    }
    @FXML
    public void handleAddItem() {
        Dialog<InvoiceLineItem> dialog = new Dialog<>();
        dialog.setTitle("Add Invoice Item");
        dialog.setHeaderText("Select product and quantity");
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        ComboBox<Product> productComboBox = new ComboBox<>();
        productComboBox.setItems(FXCollections.observableArrayList(availableProducts));
        productComboBox.setPromptText("Select product...");
        Spinner<Integer> quantitySpinner = new Spinner<>(1, 100, 1);
        quantitySpinner.setEditable(true);
        gridPane.add(new Label("Product:"), 0, 0);
        gridPane.add(productComboBox, 1, 0);
        gridPane.add(new Label("Quantity:"), 0, 1);
        gridPane.add(quantitySpinner, 1, 1);
        dialog.getDialogPane().setContent(gridPane);
        dialog.setResultConverter(dialogButton -> {
            if(dialogButton == addButtonType) {
                Product selectedProduct = productComboBox.getValue();
                if(selectedProduct != null) {
                    return new InvoiceLineItem(
                            selectedProduct,
                            quantitySpinner.getValue(),
                            selectedProduct.getDefaultPrice()
                    );
                }
            }
            return null;
        });
        dialog.showAndWait().ifPresent(lineItems::add);
    }
    @FXML
    public void handleRemoveItem() {
        InvoiceLineItem selected = itemsTable.getSelectionModel().getSelectedItem();
        if(selected != null) lineItems.remove(selected);
    }
    @FXML
    public void handleSave() {
        Customer selectedCustomer = customerComboBox.getValue();
        LocalDate dueDate = dueDatePicker.getValue();
        if (selectedCustomer == null) {
            errorLabel.setText("Please select a customer.");
            return;
        }
        if (dueDate == null) {
            errorLabel.setText("Please select a due date.");
            return;
        }
        if (lineItems.isEmpty()) {
            errorLabel.setText("Please add at least one item.");
            return;
        }
        errorLabel.setText("");
        final boolean isEditing = currentInvoice != null && currentInvoice.getId() != null;
        this.saveButton.setDisable(true);
        this.cancelButton.setDisable(true);
        if(isEditing) {
            Status selectedStatus = statusComboBox.getValue();
            if(selectedStatus != null && !selectedStatus.getId().equals(currentInvoice.getStatus().getId())) {
                new Thread(() -> {
                    boolean success = api.updateInvoiceStatus(currentInvoice.getId(), selectedStatus.getId());
                    Platform.runLater(() -> {
                        if(success) {
                            if(onSaveSuccess != null) onSaveSuccess.run();
                            Stage stage = (Stage) saveButton.getScene().getWindow();
                            stage.close();
                        }else {
                            errorLabel.setText("Failed to update invoice status.");
                            saveButton.setDisable(false);
                            cancelButton.setDisable(false);
                        }
                    });
                }).start();
            }else {
                Stage stage = (Stage) saveButton.getScene().getWindow();
                stage.close();
            }
        }else {
            new Thread(() -> {
                List<InvoiceItemRequest> items = new ArrayList<>();
                for(InvoiceLineItem item : lineItems) {
                    items.add(new InvoiceItemRequest(item.product().getId(), item.quantity()));
                }
                Invoice savedInvoice = api.createInvoice(selectedCustomer.getId(), items, dueDate);
                Platform.runLater(() -> {
                    if(savedInvoice != null) {
                        if(onSaveSuccess != null) onSaveSuccess.run();
                        Stage stage = (Stage) this.saveButton.getScene().getWindow();
                        stage.close();
                    }else {
                        errorLabel.setText("Failed to create invoice. Please try again.");
                        saveButton.setDisable(false);
                        cancelButton.setDisable(false);
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
    public void setInvoiceData(Invoice invoice) {
        this.currentInvoice = invoice;
        loadCustomers();
        loadProducts();
        loadStatuses();
        final boolean isEditing = invoice != null && invoice.getId() != null;
        if(isEditing) {
            this.customerComboBox.setValue(invoice.getCustomer());
            this.customerComboBox.setDisable(true);
            this.dueDatePicker.setValue(invoice.getDueDate());
            this.dueDatePicker.setDisable(true);
            this.addItemButton.setDisable(true);
            this.removeItemButton.setDisable(true);
            this.statusGrid.setVisible(true);
            this.statusGrid.setManaged(true);
            this.statusComboBox.setValue(invoice.getStatus());
            new Thread(() -> {
                List<InvoiceItem> invoiceItems = api.getInvoiceItems(invoice.getId());
                Platform.runLater(() -> {
                    for(InvoiceItem item : invoiceItems) {
                        this.lineItems.add(new InvoiceLineItem(
                                item.getProduct(),
                                item.getQuantity(),
                                item.getPriceAtSale()
                        ));
                    }
                    this.itemsTable.setItems(lineItems);
                });
            }).start();
        }
    }
    private void loadCustomers() {
        new Thread(() -> {
            List<Customer> customers = api.getCustomers();
            ObservableList<Customer> observableList = FXCollections.observableArrayList(customers);
            Platform.runLater(() -> this.customerComboBox.setItems(observableList));
        }).start();
    }
    private void loadProducts() {
        new Thread(() -> {
            this.availableProducts = api.getProducts();
        }).start();
    }
    private void loadStatuses() {
        new Thread(() -> {
            List<Status> statuses = api.getStatuses();
            List<Status> invoiceStatuses = statuses.stream()
                    .filter(s -> "INVOICE".equals(s.getType()))
                    .toList();
            ObservableList<Status> observableList = FXCollections.observableArrayList(invoiceStatuses);
            Platform.runLater(() -> this.statusComboBox.setItems(observableList));
        }).start();
    }
    private void updateTotalLabel() {
        BigDecimal total = BigDecimal.ZERO;
        for(InvoiceLineItem item : lineItems) {
            total = total.add(item.getLineTotal());
        }
        this.totalLabel.setText(String.format("$ %.2f", total));
    }
    public record InvoiceLineItem(Product product, int quantity, BigDecimal price) {
        public BigDecimal getLineTotal() {
                return price.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
