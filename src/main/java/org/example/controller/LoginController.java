package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.service.UserService;
import org.example.util.SceneManager;


public class LoginController {
    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblStatus;

    @FXML
    private Button loginBtn;

    private final UserService userService = new UserService();

    @FXML
    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            lblStatus.setStyle("-fx-text-fill: #F85149;");
            lblStatus.setText("Please fill in all fields!");
            return;
        }

        loginBtn.setText("Loading...");
        loginBtn.setDisable(true);

        javafx.concurrent.Task<Boolean> loginTask = new javafx.concurrent.Task<>(){
            @Override
            protected Boolean call() throws  Exception{
                return userService.authenticateUser(email, password);
            }
        };

        loginTask.setOnSucceeded(e -> {
            boolean isAuthenticated = loginTask.getValue();

            if (isAuthenticated) {
                lblStatus.setStyle("-fx-text-fill: #3FB950;");
                lblStatus.setText("Login Successful! Redirecting...");

                txtEmail.clear();
                txtPassword.clear();

                loginBtn.setText("Redirecting...");
            } else {
                loginBtn.setDisable(false);
                loginBtn.setText("Login");

                lblStatus.setStyle("-fx-text-fill: #F85149;");
                lblStatus.setText("Invalid email or password.");
            }
        });

        loginTask.setOnFailed(e -> {
            loginBtn.setDisable(false);
            loginBtn.setText("Login");

            lblStatus.setStyle("-fx-text-fill: #F85149;");
            lblStatus.setText("Invalid email or password.");
        });

        new Thread(loginTask).start();
    }

    @FXML
    private void openRegisterScreen(ActionEvent event) {
        SceneManager.switchScene(event, "/fxml/RegisterView.fxml", "CodeEvaluator - Create Account");
    }
}