package org.example.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {
    public static void switchScene(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            double width = stage.getScene().getWidth();
            double height = stage.getScene().getHeight();
            boolean isMaximized = stage.isMaximized();

            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.setTitle(title);

            if (isMaximized) {
                stage.setMaximized(true);
            }

            stage.show();

        } catch (IOException e) {
            System.err.println("Error loading FXML file: " + fxmlPath);
            e.printStackTrace();
        }
    }
}