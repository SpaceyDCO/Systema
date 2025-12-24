package com.tamv.systema.frontend;

import com.tamv.systema.frontend.API.ApiService;
import com.tamv.systema.frontend.model.RepairOrder;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;


public class RepairOrderViewController {
    private final ApiService api;
    @FXML
    public TextField searchField;
    @FXML
    public FlowPane cardsContainer;
    private List<RepairOrder> allRepairs;
    private StackPane contentArea;
    public RepairOrderViewController(ApiService api, StackPane contentArea) {
        this.api = api;
        this.contentArea = contentArea;
    }
    @FXML
    public void initialize() {
        System.out.println("Populating repair order table...");
        loadRepairs();
    }
    @FXML
    public void onAddRepair() {
        openRepairOrderForm(null);
    }
    @FXML
    public void onSearch() {
        String query = this.searchField.getText().toLowerCase().trim();
        if(query.isEmpty()) displayRepairs(allRepairs);
        else {
            List<RepairOrder> filteredOrders = this.allRepairs.stream()
                    .filter(r -> String.valueOf(r.getId()).contains(query) ||
                            r.getCustomerName().toLowerCase().contains(query) ||
                            r.getEquipmentName().toLowerCase().contains(query) ||
                            r.getStatusName().toLowerCase().contains(query))
                    .toList();
            displayRepairs(filteredOrders);
        }
    }
    private void loadRepairs() {
        new Thread(() -> {
            this.allRepairs = api.getRepairOrders();
            Platform.runLater(() -> displayRepairs(allRepairs));
        }).start();
    }
    private void displayRepairs(List<RepairOrder> orders) {
        cardsContainer.getChildren().clear();
        if(orders.isEmpty()) {
            VBox emptyState = new VBox();
            emptyState.getStyleClass().add("empty-state");
            Label emptyIcon = new Label("🔧");
            emptyIcon.getStyleClass().add("empty-state-icon");
            Label emptyTitle = new Label("No repair orders found");
            emptyTitle.getStyleClass().add("empty-state-title");
            Label emptyText = new Label("Click '+ New Repair Order' to create your first repair");
            emptyText.getStyleClass().add("empty-state-text");
            emptyState.getChildren().addAll(emptyIcon, emptyTitle, emptyText);
            cardsContainer.getChildren().add(emptyState);
            return;
        }
        for(RepairOrder order : orders) {
            VBox card = createRepairCard(order);
            cardsContainer.getChildren().add(card);
        }
    }
    private VBox createRepairCard(RepairOrder order) {
        VBox card = new VBox();
        card.getStyleClass().add("card");
        Circle circle = new Circle(35);
        circle.getStyleClass().add("profile-circle");
        Label titleLabel = new Label("Repair #" + order.getId());
        titleLabel.getStyleClass().add("card-title");
        VBox infoContainer = new VBox();
        infoContainer.getStyleClass().add("card-info");
        HBox customerRow = new HBox();
        customerRow.getStyleClass().add("info-row");
        Label customerIcon = new Label("👤");
        customerIcon.getStyleClass().add("info-icon");
        Label customerText = new Label(order.getCustomer().getFullName());
        customerText.getStyleClass().add("info-text");
        customerRow.getChildren().addAll(customerIcon, customerText);
        HBox equipmentRow = new HBox();
        equipmentRow.getStyleClass().add("info-row");
        Label equipmentIcon = new Label("📦");
        equipmentIcon.getStyleClass().add("info-icon");
        Label equipmentText = new Label(order.getEquipmentName());
        equipmentText.getStyleClass().add("info-text");
        equipmentRow.getChildren().addAll(equipmentIcon, equipmentText);
        infoContainer.getChildren().addAll(customerRow, equipmentRow);
//        if (repair.getInvoice() != null) {
//            HBox invoiceRow = new HBox();
//            invoiceRow.getStyleClass().add("info-row");
//            Label invoiceIcon = new Label("📄");
//            invoiceIcon.getStyleClass().add("info-icon");
//            Label invoiceText = new Label("Invoice #" + repair.getInvoice().getInvoiceNumber());
//            invoiceText.getStyleClass().add("info-text");
//            invoiceRow.getChildren().addAll(invoiceIcon, invoiceText);
//            infoContainer.getChildren().add(invoiceRow);
//        } FOR INVOICE LINK LATER
        Label statusBadge = new Label(order.getStatusName());
        statusBadge.getStyleClass().addAll("status-badge", getStatusClass(order.getStatusName()));
        Button detailsButton = new Button("View Details");
        detailsButton.getStyleClass().add("card-action-button");
        detailsButton.setOnAction(e -> onViewDetails(order));
        card.getChildren().addAll(circle, titleLabel, infoContainer, statusBadge, detailsButton);
        return card;
    }
    private String getStatusClass(String status) {
        return switch (status) {
            case "COMPLETED" -> "status-success";
            case "IN_PROGRESS" -> "status-warning";
            case "CANCELLED" -> "status-danger";
            default -> "status-neutral";
        };
    }
    private void onViewDetails(RepairOrder order) {
        openRepairOrderForm(order);
    }
    private void openRepairOrderForm(RepairOrder order) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/tamv/systema/frontend/repair-order-detail-view.fxml"));
            fxmlLoader.setControllerFactory(controllerClass -> new RepairOrderFormController(this.api));
            Parent view = fxmlLoader.load();
            RepairOrderFormController controller = fxmlLoader.getController();
            controller.setContentArea(this.contentArea);
            controller.setRepairOrder(order);
            Stage stage = new Stage();
            stage.setTitle(order == null ? "New Order" : "Edit Order");
            this.contentArea.getChildren().clear();
            this.contentArea.getChildren().add(view);
        }catch(IOException e) {
            e.printStackTrace();
        }
    }
    private void refreshRepairs() {
        loadRepairs();
    }
}
