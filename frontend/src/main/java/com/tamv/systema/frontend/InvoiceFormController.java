package com.tamv.systema.frontend;

import com.tamv.systema.frontend.API.ApiService;
import com.tamv.systema.frontend.Utils.General;
import com.tamv.systema.frontend.model.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InvoiceFormController {
    @FXML
    public DatePicker invoiceDatePicker;
    private final ApiService api;
    @FXML
    public ComboBox<Status> statusComboBox;
    @FXML
    public ComboBox<Customer> customerComboBox;
    @FXML
    public Label titleLabel;
    @FXML
    public Button deleteButton;
    @FXML
    public Label subtotalLabel;
    @FXML
    public Label totalLabel;
    @FXML
    public TableView<LineItemRow> lineItemsTable;
    @FXML
    public TableColumn<LineItemRow, String> productColumn;
    @FXML
    public TableColumn<LineItemRow, String> descriptionColumn;
    @FXML
    public TableColumn<LineItemRow, Integer> quantityColumn;
    @FXML
    public TableColumn<LineItemRow, Double> priceColumn;
    @FXML
    public TableColumn<LineItemRow, Double> totalColumn;
    @FXML
    public TableColumn<LineItemRow, Void> actionsColumn;
    @FXML
    public Label errorLabel;
    private Invoice invoice;
    @Setter
    private StackPane contentArea;
    private List<Product> allProducts;
    private final ObservableList<LineItemRow> lineItems;
    public InvoiceFormController(ApiService api) {
        this.api = api;
        this.lineItems = FXCollections.observableArrayList();
    }
    @FXML
    public void initialize() {
        loadStatuses();
        loadCustomers();
        loadProducts();
        setupColumns();
        this.invoiceDatePicker.setValue(LocalDate.now());
    }
    @FXML
    public void onBack() {
        goBack();
    }
    @FXML
    public void onDelete() {
        if(invoice == null || invoice.getId() == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Invoice");
        alert.setHeaderText("Are you sure you want to delete this Invoice?");
        alert.setContentText("Invoice for: " + invoice.getCustomerName() + "\nID: #" + invoice.getId() + "\nThis action cannot be undone.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                boolean success = api.deleteInvoice(invoice.getId());
                Platform.runLater(() -> {
                    if (success) goBack();
                    else General.showError(this.errorLabel, "Failed to delete Invoice. Please try again.");
                });
            }).start();
        }
    }
    @FXML
    public void onAddLineItem() {
        ChoiceDialog<Product> dialog = new ChoiceDialog<>(null, allProducts);
        dialog.setTitle("Add Line Item");
        dialog.setHeaderText("Select a product to add");
        dialog.setContentText("Product:");
        Optional<Product> result = dialog.showAndWait();
        result.ifPresent(product -> {
            lineItems.add(new LineItemRow(
                    product,
                    product.getDescription(),
                    1,
                    product.getDefaultPrice()
            ));
            updateTotals();
        });
    }
    @FXML
    public void onSave() {
        if(!validateFields()) return;
        Customer selectedCustomer = this.customerComboBox.getValue();
        LocalDate date = this.invoiceDatePicker.getValue();
        Status selectedStatus = this.statusComboBox.getValue();
        new Thread(() -> {
            try {
                boolean success;
                if(invoice == null || invoice.getId() == null) {
                    List<InvoiceItemRequest> itemRequests = new ArrayList<>();
                    for(LineItemRow row : lineItems) {
                        itemRequests.add(convertToInvoiceItemRequest(row));
                    }
                    success = api.createInvoice(selectedCustomer.getId(), itemRequests, date) != null;
                }else {
                    success = api.updateInvoiceStatus(invoice.getId(), selectedStatus.getId());
                }
                Platform.runLater(() -> {
                    if(success) goBack();
                    else General.showError(this.errorLabel, "An unexpected error happened while trying to create the invoice. Please contact an administrator");
                });
            }catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
        if(invoice == null || invoice.getId() == null) {
            this.titleLabel.setText("New Invoice");
            this.deleteButton.setVisible(false);
            this.deleteButton.setManaged(false);
            Status temp = new Status();
            temp.setName("UNPAID");
            temp.setType("INVOICE");
            this.statusComboBox.setValue(temp);
            this.statusComboBox.setDisable(true);
        }else {
            this.titleLabel.setText("Edit Invoice #" + invoice.getId());
            this.customerComboBox.setDisable(true);
            this.invoiceDatePicker.setDisable(true);
            populateFields();
        }
    }
    private InvoiceItemRequest convertToInvoiceItemRequest(LineItemRow itemRow) {
        InvoiceItemRequest request = new InvoiceItemRequest();
        request.setProductId(itemRow.getProduct().getId());
        request.setQuantity(itemRow.getQuantity());
        return request;
    }
    private boolean validateFields() {
        if (customerComboBox.getValue() == null) {
            General.showError(this.errorLabel, "Please select a customer");
            return false;
        }
        if (invoiceDatePicker.getValue() == null) {
            General.showError(this.errorLabel, "Please select an invoice date");
            return false;
        }
        if (statusComboBox.getValue() == null) {
            General.showError(this.errorLabel, "Please select a status");
            return false;
        }
        if (lineItems.isEmpty()) {
            General.showError(this.errorLabel, "Please add at least one line item");
            return false;
        }
        this.errorLabel.setVisible(false);
        this.errorLabel.setManaged(false);
        return true;
    }
    private void setupColumns() {
        this.productColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getProductName()));
        this.descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().description));
        this.quantityColumn.setCellValueFactory(cellData -> cellData.getValue().getQuantityProperty().asObject());
        this.quantityColumn.setCellFactory(column -> new TableCell<>() {
            private final Spinner<Integer> spinner = new Spinner<>(1, 999, 1);
            @Override
            protected void updateItem(Integer quantity, boolean empty) {
                super.updateItem(quantity, empty);
                if(empty) setGraphic(null);
                else {
                    LineItemRow row = getTableView().getItems().get(getIndex());
                    spinner.getValueFactory().setValue(quantity);
                    spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                        row.setQuantity(newVal);
                        updateTotals();
                    });
                    setGraphic(spinner);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        this.priceColumn.setCellValueFactory(cellData -> cellData.getValue().getPriceProperty().asObject());
        this.priceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if(empty || price == null) setText(null);
                else setText(String.format("$%.2f", price));
            }
        });
        this.totalColumn.setCellValueFactory(cellData -> cellData.getValue().getTotalProperty().asObject());
        this.totalColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                if(empty || total == null) setText(null);
                else setText(String.format("$%.2f", total));
            }
        });
        this.actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button delButton = new Button("🗑");
            {
                delButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: #ef4444; -fx-font-size: 16px;");
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if(empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) setGraphic(null);
                else {
                    delButton.setOnAction(e -> {
                        LineItemRow row = getTableView().getItems().get(getIndex());
                        lineItems.remove(row);
                        updateTotals();
                    });
                    setGraphic(delButton);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        lineItemsTable.setItems(lineItems);
    }
    private void populateFields() {
        this.invoiceDatePicker.setValue(invoice.getDueDate());
        this.statusComboBox.setValue(invoice.getStatus());
        new Thread(() -> {
            List<InvoiceItem> invoiceItems = api.getInvoiceItems(invoice.getId());
            Platform.runLater(() -> {
                if(invoiceItems != null && !invoiceItems.isEmpty()) {
                    for(InvoiceItem item : invoiceItems) {
                        lineItems.add(new LineItemRow(
                                item.getProduct(),
                                item.getProduct().getDescription(),
                                item.getQuantity(),
                                item.getPriceAtSale()
                        ));
                    }
                }
                updateTotals();
            });
        }).start();
    }
    private void loadCustomers() {
        new Thread(() -> {
            List<Customer> customers = api.getCustomers();
            ObservableList<Customer> observableList = FXCollections.observableArrayList(customers);
            Platform.runLater(() -> {
                this.customerComboBox.setItems(observableList);
                if(invoice != null && invoice.getCustomer() != null) this.customerComboBox.setValue(invoice.getCustomer());
            });
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
    private void loadProducts() {
        new Thread(() -> allProducts = api.getProducts()).start();
    }
    private void updateTotals() {
        BigDecimal subtotal = calculateTotal();
        this.subtotalLabel.setText(String.format("$%.2f", subtotal));
        this.totalLabel.setText(String.format("$%.2f", subtotal));
    }
    private BigDecimal calculateTotal() {
        double subtotal = lineItems.stream()
                .mapToDouble(LineItemRow::getLineTotal)
                .sum();
        return BigDecimal.valueOf(subtotal);
    }
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tamv/systema/frontend/invoice-view.fxml"));
            loader.setControllerFactory(controllerClass -> new InvoiceViewController(this.api, this.contentArea));
            Parent invoiceView = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(invoiceView);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Getter @AllArgsConstructor @NoArgsConstructor
    public static class LineItemRow {
        private Product product;
        private String description;
        @Setter
        private Integer quantity;
        private BigDecimal unitPrice;
        public Double getLineTotal() {
            return Double.parseDouble(unitPrice.multiply(new BigDecimal(quantity)).toString());
        }
        public String getProductName() {
            return product != null ? product.getName() : "N/A";
        }
        public SimpleIntegerProperty getQuantityProperty() {
            return new SimpleIntegerProperty(quantity);
        }
        public SimpleDoubleProperty getPriceProperty() {
            return new SimpleDoubleProperty(Double.parseDouble(unitPrice.toString()));
        }
        public SimpleDoubleProperty getTotalProperty() {
            return new SimpleDoubleProperty(getLineTotal());
        }
    }
}
