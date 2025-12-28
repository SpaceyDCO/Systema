package com.tamv.systema.frontend;

import com.tamv.systema.frontend.API.ApiService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import javax.swing.*;
import java.util.Objects;

public class LoginController {
    private final ApiService apiService;
    @FXML
    public ImageView logoImageView;
    @FXML
    public Button closeButton;
    @FXML
    public Button minimizeButton;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Label errorLabel;
    public LoginController(ApiService api) {
        this.apiService = api;
    }
    @FXML
    public void initialize() {
        try {
            Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/tamv/systema/frontend/logo.png")));
            this.logoImageView.setImage(logo);
        }catch (Exception e) {
            System.err.println("Could not load logo image");
            e.printStackTrace();
        }
    }
    @FXML
    public void onClosed(ActionEvent event) {
        Scene scene = ((Button) event.getSource()).getScene();
        ((Stage) scene.getWindow()).close();
    }
    @FXML
    public void onMinimized(ActionEvent event) {
        Scene scene = ((Button) event.getSource()).getScene();
        ((Stage) scene.getWindow()).setIconified(true);
    }
    @FXML
    protected void handleLoginButton(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        if(username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username and password cannot be empty.");
            return;
        }
        loginButton.setDisable(true);
        System.out.println("Login button clicked! Attempting to log in with user: " + username);
        new Thread(() -> {
            boolean loginSuccess = this.apiService.login(username, password);
            Platform.runLater(() -> {
                if(loginSuccess) {
                    System.out.println("Login succeeded for user: " + username);
                    errorLabel.setText("Login successful!");
                    Stage currentStage = (Stage) loginButton.getScene().getWindow();
                    MainApplication.showMainWindow(currentStage, username, this.apiService);
                }else {
                    System.out.println("Login failed for user: " + username);
                    errorLabel.setText("Login failed: Invalid credentials");
                }
                loginButton.setDisable(false);
            });
        }).start();
        errorLabel.setText("Logging in...");
    }
}
