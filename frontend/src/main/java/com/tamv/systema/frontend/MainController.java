package com.tamv.systema.frontend;

import com.tamv.systema.frontend.API.ApiService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import lombok.Setter;

import java.io.IOException;

public class MainController {
    @Setter
    private ApiService api;
    @FXML
    private AnchorPane contentArea;
    @FXML
    private Label statusBarLabel;
    public void initializeWithUsername(String username) {
        statusBarLabel.setText("Logged in as: " + username);
    }
    @FXML
    public void handleCustomersButton() {
        System.out.println("Customers button clicked");
        loadView("customer-view.fxml");
    }
    @FXML
    public void handleInvoicesButton() {
        System.out.println("Invoices button clicked");
    }
    @FXML
    public void handleRepairsButton() {
        System.out.println("Repairs button clicked");
    }
    @FXML
    public void handleProductsButton() {
        System.out.println("Products button clicked");
    }

    private void loadView(String fxmlFileName) {
        try {
            String fullPath = "/com/tamv/systema/frontend/" + fxmlFileName;
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fullPath));
            fxmlLoader.setControllerFactory(controllerClass -> {
                if(controllerClass == CustomerViewController.class) {
                    return new CustomerViewController(this.api);
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
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);
        }catch(IOException e) {
            e.printStackTrace();
            statusBarLabel.setText("Could not load view " + fxmlFileName);
        }
    }
}
