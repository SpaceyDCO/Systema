package com.tamv.systema.frontend;

import com.tamv.systema.frontend.API.ApiService;
import com.tamv.systema.frontend.model.Invoice;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InvoiceViewController {
    private final ApiService api;
    @FXML
    public TextField searchField;
    @FXML
    public FlowPane cardsContainer;
    private List<Invoice> allInvoices;
    private String currentFilter = "ALL";

    public InvoiceViewController(ApiService api) {
        this.api = api;
    }
    @FXML
    public void initialize() {
        System.out.println("Invoice view has been loaded. Populating...");
        loadInvoices();
    }
    @FXML
    public void onSearch() {
        String query = searchField.getText().toLowerCase().trim();
        if(query.isEmpty()) displayInvoices(allInvoices);
        else {
            List<Invoice> filtered = allInvoices.stream().filter(i -> i.getId().toString().contains(query) ||
                    i.getCustomerName().toLowerCase().contains(query) ||
                    String.format("%.2f", i.getTotalAmount()).contains(query) ||
                    i.getStatusName().toLowerCase().contains(query)
                    ).toList();
            displayInvoices(filtered);
        }
    }
    @FXML
    public void onFilterAll() {
        currentFilter = "ALL";
        displayInvoices(allInvoices);
    }
    @FXML
    public void onFilterPaid() {
        currentFilter = "PAID";
        displayInvoices(allInvoices);
    }
    @FXML
    public void onFilterUnpaid() {
        currentFilter = "UNPAID";
        displayInvoices(allInvoices);
    }
    @FXML
    public void onFilterOverdue() {
        currentFilter = "OVERDUE";
        displayInvoices(allInvoices);
    }
    @FXML
    public void onAddInvoice() {
        openInvoiceForm(null);
    }
    private void loadInvoices() {
        new Thread(() -> {
            this.allInvoices = api.getInvoices();
            Platform.runLater(() -> displayInvoices(allInvoices));
        }).start();
    }
    private void displayInvoices(List<Invoice> invoices) {
        cardsContainer.getChildren().clear();
        List<Invoice> filteredInvoices = applyFilter(invoices);
        if(filteredInvoices.isEmpty()) {
            VBox emptyState = new VBox();
            emptyState.getStyleClass().add("empty-state");
            Label emptyIcon = new Label("📄");
            emptyIcon.getStyleClass().add("empty-state-icon");
            Label emptyTitle = new Label("No invoices found");
            emptyTitle.getStyleClass().add("empty-state-title");
            Label emptyText = new Label("Click '+ Create Invoice' to create your first invoice");
            emptyText.getStyleClass().add("empty-state-text");
            emptyState.getChildren().addAll(emptyIcon, emptyTitle, emptyText);
            cardsContainer.getChildren().add(emptyState);
            return;
        }
        for(Invoice invoice : filteredInvoices) {
            VBox card = createInvoiceCard(invoice);
            cardsContainer.getChildren().add(card);
        }
    }
    private List<Invoice> applyFilter(List<Invoice> invoices) {
        return switch(currentFilter) {
            case "PAID" -> invoices.stream()
                    .filter(i -> "PAID".equalsIgnoreCase(i.getStatusName()))
                    .toList();
            case "UNPAID" -> invoices.stream()
                    .filter(i -> "UNPAID".equalsIgnoreCase(i.getStatusName()))
                    .toList();
            case "OVERDUE" -> invoices.stream()
                    .filter(i -> "OVERDUE".equalsIgnoreCase(i.getStatusName()))
                    .toList();
            default -> invoices;
        };
    }
    private VBox createInvoiceCard(Invoice invoice) {
        VBox card = new VBox();
        card.getStyleClass().add("card");
        Circle circle = new Circle(35);
        circle.getStyleClass().add("profile-circle");
        Label titleLabel = new Label("Invoice #" + invoice.getId());
        titleLabel.getStyleClass().add("card-title");
        VBox infoContainer = new VBox();
        infoContainer.getStyleClass().add("card-info");
        HBox customerRow = new HBox();
        customerRow.getStyleClass().add("info-row");
        Label customerIcon = new Label("👤");
        customerIcon.getStyleClass().add("info-icon");
        Label customerText = new Label(invoice.getCustomer().getFullName());
        customerText.getStyleClass().add("info-text");
        customerRow.getChildren().addAll(customerIcon, customerText);
        HBox dateRow = new HBox();
        dateRow.getStyleClass().add("info-row");
        Label dateIcon = new Label("📅");
        dateIcon.getStyleClass().add("info-icon");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        Label dateText = new Label(invoice.getInvoiceDate().format(formatter));
        dateText.getStyleClass().add("info-text");
        dateRow.getChildren().addAll(dateIcon, dateText);
        HBox amountRow = new HBox();
        amountRow.getStyleClass().add("info-row");
        Label amountIcon = new Label("💵");
        amountIcon.getStyleClass().add("info-icon");
        Label amountText = new Label(String.format("$%.2f", invoice.getTotalAmount()));
        amountText.getStyleClass().add("info-text");
        amountRow.getChildren().addAll(amountIcon, amountText);
        infoContainer.getChildren().addAll(customerRow, dateRow, amountRow);
        Label statusBadge = new Label(invoice.getStatusName());
        statusBadge.getStyleClass().addAll("status-badge", getStatusClass(invoice.getStatusName()));
        Button detailsButton = new Button("View Details");
        detailsButton.getStyleClass().add("card-action-button");
        detailsButton.setOnAction(e -> onViewDetails(invoice));
        card.getChildren().addAll(circle, titleLabel, infoContainer, statusBadge, detailsButton);
        return card;
    }
    private String getStatusClass(String status) {
        return switch (status) {
            case "PAID" -> "status-success";
            case "UNPAID" -> "status-warning";
            case "OVERDUE" -> "status-danger";
            default -> "status-neutral";
        };
    }
    private void onViewDetails(Invoice invoice) {
        openInvoiceForm(invoice);
    }
    private void openInvoiceForm(Invoice invoice) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tamv/systema/frontend/invoice-form.fxml"));
            Parent popup = loader.load();
            InvoiceFormController controller = loader.getController();
            controller.setApi(this.api);
            controller.setOnSaveSuccess(this::refreshInvoices);
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
    private void refreshInvoices() {
        loadInvoices();
    }
}
