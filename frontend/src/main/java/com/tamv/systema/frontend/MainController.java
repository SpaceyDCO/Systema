package com.tamv.systema.frontend;

import com.tamv.systema.frontend.API.ApiService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.StackPane;
import lombok.Setter;

import java.io.IOException;

public class MainController {
    @FXML
    public Button customersButton;
    @FXML
    public Button invoicesButton;
    @FXML
    public Button repairsButton;
    @FXML
    public Button productsButton;
    @FXML
    public MenuButton userMenuButton;
    @Setter
    private ApiService api;
    @FXML
    private StackPane contentArea;
    @FXML
    private Button homeButton;
    private Button currentSelectedButton;
    public void initializeWithUsername(String username) {
        this.userMenuButton.setText("\uD83D\uDC64 " + username);
    }
    @FXML
    public void initialize() {
        setSelectedButton(homeButton);
        loadView("dashboard-view.fxml");
    }
    @FXML
    public void onHomeClicked() {
        setSelectedButton(homeButton);
        loadView("dashboard-view.fxml");
    }
    @FXML
    public void handleCustomersButton() {
        System.out.println("Customers button clicked");
        setSelectedButton(customersButton);
        loadView("customer-view.fxml");
    }
    @FXML
    public void handleInvoicesButton() {
        System.out.println("Invoices button clicked");
        setSelectedButton(invoicesButton);
        loadView("invoice-view.fxml");
    }
    @FXML
    public void handleRepairsButton() {
        System.out.println("Repairs button clicked");
        setSelectedButton(repairsButton);
        loadView("repair-order-view.fxml");
    }
    @FXML
    public void handleProductsButton() {
        System.out.println("Products button clicked");
        setSelectedButton(productsButton);
        loadView("product-view.fxml");
    }

    private void loadView(String fxmlFileName) {
        try {
            String fullPath = "/com/tamv/systema/frontend/" + fxmlFileName;
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fullPath));
            fxmlLoader.setControllerFactory(controllerClass -> {
                if(controllerClass == CustomerViewController.class) {
                    return new CustomerViewController(this.api, this.contentArea);
                }if(controllerClass == ProductViewController.class) {
                    return new ProductViewController(this.api);
                }if(controllerClass == RepairOrderViewController.class) {
                    return new RepairOrderViewController(this.api);
                }if(controllerClass == InvoiceViewController.class) {
                    return new InvoiceViewController(this.api);
                }else {
                    try {
                        return controllerClass.getDeclaredConstructor().newInstance();
                    }catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            Parent view = fxmlLoader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        }catch(IOException e) {
            e.printStackTrace();
        }
    }
    private void setSelectedButton(Button button) {
        if(currentSelectedButton != null) {
            currentSelectedButton.getStyleClass().remove("selected");
        }
        button.getStyleClass().add("selected");
        this.currentSelectedButton = button;
    }
}
