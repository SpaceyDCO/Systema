package com.tamv.systema.frontend;

import com.tamv.systema.frontend.API.ApiService;
import com.tamv.systema.frontend.model.Product;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.util.List;

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
    private void refreshTable() {
        new Thread(() -> {
            List<Product> products = api.getProducts();
            ObservableList<Product> observableList = FXCollections.observableArrayList(products);
            Platform.runLater(() -> productTable.setItems(observableList));
        }).start();
    }
}
