package com.tamv.systema.frontend;

import com.tamv.systema.frontend.API.ApiService;
import com.tamv.systema.frontend.model.Product;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

public class ProductFormController {
    private final ApiService api;
    private final StackPane contentArea;
    @FXML
    public Label errorLabel;
    @FXML
    public TextField nameField;
    @FXML
    public TextField priceField;
    @FXML
    public TextField categoryField;
    @FXML
    public TextArea descriptionArea;
    @FXML
    public Label titleLabel;
    @FXML
    public Button deleteButton;
    private Product product;
    public ProductFormController(ApiService api, StackPane contentArea) {
        this.api = api;
        this.contentArea = contentArea;
    }
    @FXML
    public void onBack() {
        goBack();
    }
    @FXML
    public void onSave() {
        if(!validateFields()) return;
        String name = this.nameField.getText().trim();
        String price = this.priceField.getText().trim();
        String category = this.categoryField.getText().trim();
        String description = this.descriptionArea.getText().trim();
        new Thread(() -> {
            try {
                boolean success;
                if(product == null || product.getId() == null) {
                    Product newProduct = new Product();
                    newProduct.setName(name);
                    newProduct.setDefaultPrice(new BigDecimal(price));
                    newProduct.setCategory(category);
                    newProduct.setDescription(description);
                    success = api.createProduct(newProduct) != null;
                }else {
                    product.setName(name);
                    product.setDefaultPrice(new BigDecimal(price));
                    product.setCategory(category);
                    product.setDescription(description);
                    success = api.updateProduct(product.getId(), product);
                }
                Platform.runLater(() -> {
                    if(success) goBack();
                    else showError("Failed to save product. Please try again.");
                });
            }catch (Exception e) {
                Platform.runLater(() -> showError("An unexpected error happened while trying to save the product. Please contact an administrator"));
            }
        }).start();
    }
    @FXML
    public void onDelete() {
        if(product == null || product.getId() == null) return; //Fail-safe
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Product");
        alert.setHeaderText("Are you sure you want to delete this product?");
        alert.setContentText("Product: " + product.getName() + "\nThis action cannot be undone.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                boolean success = api.deleteProduct(product.getId());
                Platform.runLater(() -> {
                    if (success) goBack();
                    else showError("Failed to delete product. Please try again.");
                });
            }).start();
        }
    }
    public void setProduct(Product product) {
        this.product = product;
        if(product == null || product.getId() == null) {
            this.titleLabel.setText("New Product");
            this.deleteButton.setVisible(false);
            this.deleteButton.setManaged(false);
        }else {
            this.titleLabel.setText("Edit Product");
            populateFields();
        }
    }
    private void populateFields() {
        this.nameField.setText(product.getName());
        this.priceField.setText(product.getDefaultPrice().toString());
        this.categoryField.setText(product.getCategory());
        this.descriptionArea.setText(product.getDescription());
    }
    private boolean validateFields() {
        String name = this.nameField.getText().trim();
        String price = this.priceField.getText().trim();
        String category = this.categoryField.getText().trim();
        String description = this.descriptionArea.getText().trim();
        if(name.isEmpty()) {
            showError("Name is required");
            return false;
        }
        if(price.isEmpty()) {
            showError("Price is required");
            return false;
        }
        try {
            new BigDecimal(price);
        }catch (NumberFormatException e) {
            showError("Invalid price formatting. Do not include currency symbols");
            return false;
        }
        if(category.isEmpty()) {
            showError("Category is required");
            return false;
        }
        if(description.isEmpty()) {
            showError("Description is required");
            return false;
        }
        this.errorLabel.setVisible(false);
        this.errorLabel.setManaged(false);
        return true;
    }
    private void showError(String error) {
        this.errorLabel.setText(error);
        this.errorLabel.setVisible(true);
        this.errorLabel.setManaged(true);
    }
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tamv/systema/frontend/product-view.fxml"));
            loader.setControllerFactory(controllerClass -> new ProductViewController(this.api, this.contentArea));
            Parent productView = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(productView);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
