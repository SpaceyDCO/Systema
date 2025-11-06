package com.tamv.systema.frontend;

import com.tamv.systema.frontend.API.AuthService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    private final AuthService authService = new AuthService();
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Label errorLabel;
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
            boolean loginSuccess = this.authService.login(username, password);
            Platform.runLater(() -> {
                if(loginSuccess) {
                    System.out.println("Login succeeded for user: " + username);
                    errorLabel.setText("Login successful!");
                    //Close window later and open new
                }else {
                    System.out.println("Login failed for user: " + username);
                    errorLabel.setText("Login failed: Invalid credentials");
                }
                loginButton.setDisable(false);
            });
        }).start();
        //Add API Check
        errorLabel.setText("Logging in...");
    }
}
