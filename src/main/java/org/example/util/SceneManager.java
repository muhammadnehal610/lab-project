package org.example.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Stack;
import java.util.function.Consumer;

public class SceneManager {
    private static final Stack<SceneHistory> historyStack = new Stack<>();

    private static class SceneHistory {
        Scene scene;
        String title;

        public SceneHistory(Scene scene, String title) {
            this.scene = scene;
            this.title = title;
        }
    }

    public static <T> void switchScene(ActionEvent event, String fxmlPath, String title , Consumer<T> controllerSetup) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage;
            Object source = event.getSource();

            if (source instanceof Node) {
                stage = (Stage) ((Node) source).getScene().getWindow();
            } else if (source instanceof MenuItem menuItem) {
                stage = (Stage) menuItem.getParentPopup().getOwnerWindow();
            } else {
                throw new IllegalArgumentException("Unknown event source type");
            }

            if (stage.getScene() != null) {
                historyStack.push(new SceneHistory(stage.getScene(), stage.getTitle()));
            }

            double width = stage.getScene() != null ? stage.getScene().getWidth() : 800;
            double height = stage.getScene() != null ? stage.getScene().getHeight() : 600;
            boolean isMaximized = stage.isMaximized();

            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.setTitle(title);

            if (isMaximized) {
                stage.setMaximized(true);
            }

            if (controllerSetup != null) {
                T controller = loader.getController();
                controllerSetup.accept(controller);
            }

            stage.show();

        } catch (IOException e) {
            System.err.println("Error loading FXML file: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public static void goBack(ActionEvent event) {
        if (historyStack.isEmpty()) {
            System.out.println("Koi pichli screen mojood nahi hai!");
            return;
        }

        Stage stage;
        Object source = event.getSource();

        if (source instanceof Node) {
            stage = (Stage) ((Node) source).getScene().getWindow();
        } else if (source instanceof MenuItem menuItem) {
            stage = (Stage) menuItem.getParentPopup().getOwnerWindow();
        } else {
            throw new IllegalArgumentException("Unknown event source type");
        }

        SceneHistory previousScreen = historyStack.pop();
        boolean isMaximized = stage.isMaximized();

        stage.setScene(previousScreen.scene);
        stage.setTitle(previousScreen.title);

        if (isMaximized) {
            stage.setMaximized(true);
        }

        stage.show();
    }

    public static void switchScene(ActionEvent event, String fxmlPath, String title) {
        switchScene(event, fxmlPath, title, null);
    }

    public static void redirectToLogin(ActionEvent event) {
        switchScene(event, "/fxml/LoginView.fxml", "CodeEvaluator AI - Login");
    }

    public static void redirectToAdmin(ActionEvent event) {
        switchScene(event, "/fxml/admin/AdminDashboardView.fxml", "CodeEvaluator AI - Admin Dashboard");
    }

    public static void redirectToStudent(ActionEvent event) {
        switchScene(event, "/fxml/student/StudentDashboardView.fxml", "CodeEvaluator AI - Student Dashboard");
    }
}