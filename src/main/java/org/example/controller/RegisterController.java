package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.util.SceneManager;

import java.io.IOException;


public class RegisterController {

    @FXML
    private  TextField txtFirstName;

    @FXML
    private  TextField txtLastName;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblStatus;

    @FXML
    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            lblStatus.setStyle("-fx-text-fill: #F85149;");
            lblStatus.setText("Please fill in all fields!");
            return;
        }

        if (email.equals("teacher@evaluator.com") && password.equals("admin123")) {
            lblStatus.setStyle("-fx-text-fill: #3FB950;"); // Green Success
            lblStatus.setText("Login Successful! Redirecting...");

            // Redirect to Dashboard logic comes here
            System.out.println("User authenticated successfully as Teacher.");
        } else {
            lblStatus.setStyle("-fx-text-fill: #F85149;");
            lblStatus.setText("Invalid email or password.");
        }
    }


    @FXML
    private void openLoginScreen(ActionEvent event) {
        SceneManager.switchScene(event, "/fxml/LoginView.fxml", "CodeEvaluator - Sign In");
    }

}
