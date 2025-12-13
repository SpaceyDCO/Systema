package com.tamv.systema.frontend;

import com.tamv.systema.frontend.API.ApiService;
import com.tamv.systema.frontend.model.Customer;
import com.tamv.systema.frontend.model.Product;
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
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProductViewController {
    private final ApiService api;
    @FXML
    public TableView<Product> productTable;
    @FXML
    public TableColumn<Product, Long> idColumn;
    @FXML
    public TableColumn<Product, String> nameColumn;
    @FXML
    public TableColumn<Product, String> descriptionColumn;
    @FXML
    public TableColumn<Product, BigDecimal> priceColumn;
    @FXML
    public TableColumn<Product, String> categoryColumn;
    @FXML
    public Button newButton;
    @FXML
    public Button editButton;
    @FXML
    public Button deleteButton;
    public ProductViewController(ApiService api) {
        this.api = api;
    }

    @FXML
    public void initialize() {
        System.out.println("Product view has been loaded... Populating table");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("defaultPrice"));
        priceColumn.setCellFactory(column -> new TableCell<Product, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                if(empty || price == null) {
                    setText(null);
                }else {
                    setText(String.format("$ %.2f", price));
                }
            }
        });
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        new Thread(() -> {
            List<Product> products = api.getProducts();
            ObservableList<Product> observableList = FXCollections.observableArrayList(products);
            Platform.runLater(() -> productTable.setItems(observableList));
        }).start();
        this.editButton.setDisable(true);
        this.deleteButton.setDisable(true);
        this.productTable.getSelectionModel().selectedItemProperty().addListener((observable, oldSelection, newSelection) -> {
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
    public void handleNewProduct(ActionEvent event) {
        openCustomerForm(null);
    }
    @FXML
    public void handleEdit(ActionEvent event) {
        Product selectedProduct = this.productTable.getSelectionModel().getSelectedItem();
        if(selectedProduct == null) return;
        openCustomerForm(selectedProduct);
    }
    @FXML
    public void handleDelete(ActionEvent event) {
        Product selectedProduct = this.productTable.getSelectionModel().getSelectedItem();
        if(selectedProduct == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete product");
        alert.setHeaderText("Are you sure you want to delete this product?");
        alert.setContentText("Product: " + selectedProduct.getName() + "\nThis action cannot be undone.");
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            System.out.println("User confirmed deletion for product " + selectedProduct.getName() + " with id " + selectedProduct.getId());
            new  Thread(() -> {
                boolean success = api.deleteProduct(selectedProduct.getId());
                Platform.runLater(() -> {
                    if(success) {
                        refreshTable();
                    }else {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Deletion failed");
                        errorAlert.setHeaderText("Could not delete product");
                        errorAlert.setContentText("The product could not be deleted from the database. Please try again or contact an administrator");
                        errorAlert.showAndWait();
                    }
                });
            }).start();
        }else {
            System.out.println("User canceled deletion");
        }
    }
    private void openCustomerForm(Product product) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/tamv/systema/frontend/product-form.fxml"));
            Parent popup = fxmlLoader.load();
            ProductFormController controller = fxmlLoader.getController();
            controller.setProductData(product);
            controller.setApi(this.api);
            controller.setOnSaveSuccess(this::refreshTable);
            Stage stage = new Stage();
            stage.setTitle(product == null ? "New Customer" : "Edit Customer");
            stage.setScene(new Scene(popup));
            stage.setAlwaysOnTop(true);
            stage.setResizable(false);
            stage.showAndWait();
        }catch(IOException e) {
            e.printStackTrace();
        }
    }
    private void refreshTable() {
        new Thread(() -> {
            List<Product> products = api.getProducts();
            ObservableList<Product> observableList = FXCollections.observableArrayList(products);
            Platform.runLater(() -> productTable.setItems(observableList));
        }).start();
    }
}
