package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.example.model.User;
import org.example.util.SceneManager;
import org.example.util.SessionManager;
import org.example.util.UserSession;

import java.io.IOException;
import java.io.InputStream;

public class AdminDashboardController {
    @FXML
    private Button btnProblems;

    @FXML
    private Button btnCategories;

    @FXML
    private StackPane contentArea;

    @FXML
    private Label headerUserName;

    User currentUser = UserSession.getInstance().getCurrentUser();

    @FXML
    public void initialize() {
        handleLoadProblems();

        headerUserName.setText(currentUser.getFullName());
    }

    @FXML
    public void handleLoadProblems() {
        setActiveButton(btnProblems);
        loadSubView("/fxml/admin/ProblemsView.fxml");
    }

    @FXML
    public void handleLoadCategories() {
        setActiveButton(btnCategories);
        loadSubView("/fxml/admin/CategoriesView.fxml");
    }

    private void loadSubView(String fxmlPath) {
        try {
            Node node = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button clickedButton) {
        btnProblems.getStyleClass().remove("active");
        btnCategories.getStyleClass().remove("active");

        if (!clickedButton.getStyleClass().contains("active")) {
            clickedButton.getStyleClass().add("active");
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        SessionManager.clearSession();
        UserSession.getInstance().logout();
        SceneManager.redirectToLogin(event);
    }
}
