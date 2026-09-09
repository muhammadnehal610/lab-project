package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.service.UserService;
import org.example.util.SceneManager;

public class RegisterController {

    @FXML
    private TextField txtFirstName;

    @FXML
    private TextField txtLastName;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblStatus;

    @FXML
    private Button registerBtn;

    private final UserService userService = new UserService();

    @FXML
    private void handleRegister(ActionEvent event) {
        String firstName = txtFirstName.getText();
        String lastName = txtLastName.getText();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            lblStatus.getStyleClass().removeAll("status-success");
            lblStatus.getStyleClass().add("status-error");
            lblStatus.setText("Please fill in all fields.");
            return;
        }

        registerBtn.setText("Loading...");
        registerBtn.setDisable(true);

        javafx.concurrent.Task<Boolean> registerTask = new javafx.concurrent.Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return userService.registerUser(firstName, lastName, email, password);
            }
        };

        registerTask.setOnSucceeded(e -> {
            boolean isRegistered = registerTask.getValue();

            if (isRegistered) {
                lblStatus.getStyleClass().removeAll("status-error");
                lblStatus.getStyleClass().add("status-success");
                lblStatus.setText("Account created successfully! Please Login to continue");

                txtFirstName.clear();
                txtLastName.clear();
                txtEmail.clear();
                txtPassword.clear();

                registerBtn.setText("Redirecting...");

                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
                delay.setOnFinished(ev -> SceneManager.redirectToLogin(event));
                delay.play();
            } else {
                registerBtn.setDisable(false);
                registerBtn.setText("Sign Up");

                lblStatus.getStyleClass().removeAll("status-success");
                lblStatus.getStyleClass().add("status-error");
                lblStatus.setText("Registration failed: Email already exists or invalid data.");
            }
        });

        registerTask.setOnFailed(e -> {
            registerBtn.setDisable(false);
            registerBtn.setText("Sign Up");
            lblStatus.getStyleClass().removeAll("status-success");
            lblStatus.getStyleClass().add("status-error");
            lblStatus.setText("An unexpected error occurred.");
        });

        new Thread(registerTask).start();
    }


    @FXML
    private void openLoginScreen(ActionEvent event) {
        SceneManager.switchScene(event, "/fxml/LoginView.fxml", "CodeEvaluator - Sign In");
    }

}