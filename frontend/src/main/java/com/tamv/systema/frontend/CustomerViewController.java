package com.tamv.systema.frontend;

import com.tamv.systema.frontend.API.ApiService;
import com.tamv.systema.frontend.model.Customer;
import javafx.animation.PauseTransition;
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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CustomerViewController {
    private final ApiService api;
    @FXML
    public TextField searchField;
    @FXML
    public FlowPane cardsContainer;
    private List<Customer> allCustomers;
    public CustomerViewController(ApiService api) {
        this.api = api;
    }
    @FXML
    public void initialize() {
        System.out.println("Customer view has been loaded... Populating table");
        loadCustomers();
    }
    @FXML
    public void onViewDetails(Customer customer) {
        openCustomerForm(customer);
    }
    @FXML
    public void onAddCustomer() {
        openCustomerForm(null);
    }
    @FXML
    public void onSearch() {
        String query = searchField.getText().toLowerCase().trim();
        if(query.isEmpty()) {
            displayCustomers(allCustomers);
        }else {
            List<Customer> filtered = allCustomers.stream()
                    .filter(c -> c.getFullName().toLowerCase().contains(query) ||
                            c.getEmail().toLowerCase().contains(query) ||
                            c.getPhoneNumber().contains(query))
                    .toList();
            displayCustomers(filtered);
        }
    }
    private void loadCustomers() {
        new Thread(() -> {
            this.allCustomers = api.getCustomers();
            Platform.runLater(() -> displayCustomers(allCustomers));
        }).start();
    }
    private void displayCustomers(List<Customer> customers) {
        this.cardsContainer.getChildren().clear();
        if(customers.isEmpty()) {
            VBox emptyState = new VBox();
            emptyState.getStyleClass().add("empty-state");
            Label emptyIcon = new Label("📭");
            emptyIcon.getStyleClass().add("empty-state-icon");
            Label emptyTitle = new Label("No customers found");
            emptyTitle.getStyleClass().add("empty-state-title");
            Label emptyText = new Label("Click '+ Add Customer' to create your first customer");
            emptyText.getStyleClass().add("empty-state-text");
            emptyState.getChildren().addAll(emptyIcon, emptyTitle, emptyText);
            cardsContainer.getChildren().add(emptyState);
            return;
        }
        for(Customer customer : customers) {
            VBox card = createCustomerCard(customer);
            cardsContainer.getChildren().add(card);
        }
    }
    private VBox createCustomerCard(Customer customer) {
        VBox card = new VBox();
        card.getStyleClass().add("card");
        Circle circle = new Circle(35);
        circle.getStyleClass().add("profile-circle");
        Label nameLabel = new Label(customer.getFullName());
        nameLabel.getStyleClass().add("card-title");
        VBox contactInfo = new VBox();
        contactInfo.getStyleClass().add("card-info");
        HBox emailRow = new HBox();
        emailRow.getStyleClass().add("info-row");
        Label emailIcon = new Label("📧");
        emailIcon.getStyleClass().add("info-icon");
        Label emailText = new Label(customer.getEmail());
        emailText.getStyleClass().addAll("info-text", "copyable");
        emailText.setOnMouseClicked(e -> copyToClipboard(customer.getEmail(), emailText));
        emailRow.getChildren().addAll(emailIcon, emailText);
        HBox phoneRow = new HBox();
        phoneRow.getStyleClass().add("info-row");
        Label phoneIcon = new Label("☎");
        phoneIcon.getStyleClass().add("info-icon");
        Label phoneText = new Label(customer.getPhoneNumber());
        phoneText.getStyleClass().addAll("info-text", "copyable");
        phoneText.setOnMouseClicked(e -> copyToClipboard(customer.getPhoneNumber(), phoneText));
        phoneRow.getChildren().addAll(phoneIcon, phoneText);
        contactInfo.getChildren().addAll(emailRow, phoneRow);
        Button detailsButton = new Button("View Details");
        detailsButton.getStyleClass().add("card-action-button");
        detailsButton.setOnAction(e -> onViewDetails(customer));
        card.getChildren().addAll(circle, nameLabel, contactInfo, detailsButton);
        return card;
    }
    private void openCustomerForm(Customer customer) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/tamv/systema/frontend/customer-form.fxml"));
            Parent popup = fxmlLoader.load();
            CustomerFormController controller = fxmlLoader.getController();
            controller.setCustomerData(customer);
            controller.setApi(this.api);
            controller.setOnSaveSuccess(this::refreshCustomers);
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
    private void refreshCustomers() {
        loadCustomers();
    }
    private void copyToClipboard(String text, Label label) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
        showCopiedFeedback(label);
    }
    private void showCopiedFeedback(Label label) {
        String originalText = label.getText();
        label.setText("✓ Copied!");
        label.getStyleClass().add("copied-feedback");
        PauseTransition pauseTransition = new PauseTransition(Duration.seconds(1.5));
        pauseTransition.setOnFinished(e -> {
            label.setText(originalText);
            label.getStyleClass().remove("copied-feedback");
        });
        pauseTransition.play();
    }
}
