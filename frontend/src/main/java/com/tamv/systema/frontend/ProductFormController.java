package com.tamv.systema.frontend;

import com.tamv.systema.frontend.API.ApiService;
import com.tamv.systema.frontend.model.Product;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Setter;

import java.math.BigDecimal;

public class ProductFormController {
    @FXML
    public TextField nameField;
    @FXML
    public TextField priceField;
    @FXML
    public TextField categoryField;
    @FXML
    public TextArea descriptionField;
    @FXML
    public Label errorLabel;
    @FXML
    public Button saveButton;
    @FXML
    public Button cancelButton;
    @Setter
    private Runnable onSaveSuccess;
    @Setter
    private ApiService api;
    private Product currentProduct;
    @FXML
    public void handleSave(ActionEvent event) {
        String name = this.nameField.getText();
        String priceText = this.priceField.getText();
        String description = this.descriptionField.getText();
        String category = this.categoryField.getText();
        if(name == null || name.trim().isEmpty()) {
            this.errorLabel.setText("Product name is required.");
            return;
        }
        if(priceText == null || priceText.trim().isEmpty()) {
            this.errorLabel.setText("Price is required.");
            return;
        }
        BigDecimal price;
        try {
            price = new BigDecimal(priceText);
            if(price.compareTo(BigDecimal.ZERO) <= 0) {
                this.errorLabel.setText("Price must be positive.");
                return;
            }
        }catch(NumberFormatException e) {
            this.errorLabel.setText("Invalid price format.");
            return;
        }
        if(category == null || category.trim().isEmpty()) {
            this.errorLabel.setText("Category is required.");
            return;
        }
        this.errorLabel.setText("");
        final boolean isEditing = this.currentProduct != null && this.currentProduct.getId() != null;
        if(this.currentProduct == null) {
            this.currentProduct = new Product();
        }
        this.currentProduct.setName(name);
        this.currentProduct.setDescription(description);
        this.currentProduct.setCategory(category);
        this.currentProduct.setDefaultPrice(price);
        this.saveButton.setDisable(true);
        this.cancelButton.setDisable(true);
        new Thread(() -> {
            Product savedProduct = isEditing ? api.updateProduct(this.currentProduct.getId(), this.currentProduct) : api.createProduct(this.currentProduct);
            Platform.runLater(() -> {
                if(savedProduct != null) {
                    if(this.onSaveSuccess != null) {
                        this.onSaveSuccess.run();
                    }
                    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
                    stage.close();
                }else {
                    this.errorLabel.setText("Failed to save product, please try again...");
                    this.saveButton.setDisable(false);
                    this.cancelButton.setDisable(false);
                }
            });
        }).start();
    }
    @FXML
    public void handleCancel(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }
    public void setProductData(Product product) {
        this.currentProduct = product;
        if(product != null) {
            this.nameField.setText(product.getName());
            this.categoryField.setText(product.getCategory());
            this.descriptionField.setText(product.getDescription());
            if(product.getDefaultPrice() != null) {
                this.priceField.setText(product.getDefaultPrice().toString());
            }
        }
    }
}
