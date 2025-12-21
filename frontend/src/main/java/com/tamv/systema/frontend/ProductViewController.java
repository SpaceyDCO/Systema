package com.tamv.systema.frontend;

import com.tamv.systema.frontend.API.ApiService;
import com.tamv.systema.frontend.model.Product;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.util.List;

public class ProductViewController {
    private final ApiService api;
    @FXML
    public TextField searchField;
    @FXML
    public FlowPane cardsContainer;
    private List<Product> allProducts;
    private final StackPane contentArea;
    public ProductViewController(ApiService api, StackPane contentArea) {
        this.api = api;
        this.contentArea = contentArea;
    }

    @FXML
    public void initialize() {
        System.out.println("Product view has been loaded... Populating table");
        loadProducts();
    }
    @FXML
    public void onSearch() {
        String query = searchField.getText().toLowerCase().trim();
        if(query.isEmpty()) {
            displayProducts(allProducts);
        }else {
            List<Product> filtered = allProducts.stream()
                    .filter(c -> c.getName().toLowerCase().contains(query) ||
                            c.getCategory().toLowerCase().contains(query))
                    .toList();
            displayProducts(filtered);
        }
    }
    @FXML
    public void handleNewProduct(ActionEvent event) {
        openProductForm(null);
    }
    private void displayProducts(List<Product> products) {
        this.cardsContainer.getChildren().clear();
        if(this.allProducts.isEmpty()) {
            VBox emptyState = new VBox();
            emptyState.getStyleClass().add("empty-state");
            Label emptyIcon = new Label("📭");
            emptyIcon.getStyleClass().add("empty-state-icon");
            Label emptyTitle = new Label("No products found");
            emptyTitle.getStyleClass().add("empty-state-title");
            Label emptyText = new Label("Click '+ Add Product' to create your first product");
            emptyText.getStyleClass().add("empty-state-text");
            emptyState.getChildren().addAll(emptyIcon, emptyTitle, emptyText);
            cardsContainer.getChildren().add(emptyState);
            return;
        }
        for(Product product : products) {
            VBox card = createProductCard(product);
            cardsContainer.getChildren().add(card);
        }
    }
    private VBox createProductCard(Product product) {
        VBox card = new VBox();
        card.getStyleClass().add("card");
        Circle circle = new Circle(35);
        circle.getStyleClass().add("profile-circle");
        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("card-title");
        VBox detailInfo = new VBox();
        detailInfo.getStyleClass().add("card-info");
        HBox categoryRow = new HBox();
        categoryRow.getStyleClass().add("info-row");
        Label categoryIcon = new Label("🔧");
        categoryIcon.getStyleClass().add("info-icon");
        Label categoryText = new Label(product.getCategory());
        categoryText.getStyleClass().addAll("info-text");
        categoryRow.getChildren().addAll(categoryIcon, categoryText);
        HBox priceRow = new HBox();
        priceRow.getStyleClass().add("info-row");
        Label priceIcon = new Label("💵");
        priceIcon.getStyleClass().add("info-icon");
        Label priceText = new Label(product.getDefaultPrice().toString());
        priceText.getStyleClass().addAll("info-text");
        priceRow.getChildren().addAll(priceIcon, priceText);
        detailInfo.getChildren().addAll(categoryRow, priceRow);
        Button detailsButton = new Button("View Details");
        detailsButton.getStyleClass().add("card-action-button");
        detailsButton.setOnAction(e -> onViewDetails(product));
        card.getChildren().addAll(circle, nameLabel, detailInfo, detailsButton);
        return card;
    }
    private void onViewDetails(Product product) {
        openProductForm(product);
    }
    private void openProductForm(Product product) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/tamv/systema/frontend/product-detail-view.fxml"));
            fxmlLoader.setControllerFactory(controlledClass -> new ProductFormController(this.api));
            Parent detailView = fxmlLoader.load();
            ProductFormController controller = fxmlLoader.getController();
            controller.setContentArea(this.contentArea);
            controller.setProduct(product);
            this.contentArea.getChildren().clear();
            this.contentArea.getChildren().add(detailView);

        }catch(IOException e) {
            e.printStackTrace();
        }
    }
    private void loadProducts() {
        new Thread(() -> {
            this.allProducts = api.getProducts();
            Platform.runLater(() -> displayProducts(this.allProducts));
        }).start();
    }
}
