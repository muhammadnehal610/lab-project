package org.example;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.util.DatabaseConnection;
import org.example.util.SessionManager;
import org.example.util.UserSession;

import java.util.Objects;

public class MainApp extends Application {

    private final UserRepository userRepository = new UserRepository();

    private static class InitResult {
        final Scene scene;
        final String title;

        InitResult(Scene scene, String title) {
            this.scene = scene;
            this.title = title;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            DatabaseConnection.initializeDatabase();

            Font.loadFont(getClass().getResourceAsStream("/fonts/Inter_18pt-Regular.ttf"), 14);
            Font.loadFont(getClass().getResourceAsStream("/fonts/Inter_18pt-Medium.ttf"), 14);
            Font.loadFont(getClass().getResourceAsStream("/fonts/Inter_18pt-SemiBold.ttf"), 14);
            Font.loadFont(getClass().getResourceAsStream("/fonts/Inter_18pt-Bold.ttf"), 14);

            Stage splashStage = new Stage();
            splashStage.initStyle(StageStyle.UNDECORATED);

            Image appIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/app-icon.png")));

            ImageView splashIconView = new ImageView(appIcon);
            splashIconView.setFitWidth(110);
            splashIconView.setFitHeight(110);
            splashIconView.setPreserveRatio(true);

            Label titleLabel = new Label("CodeEvaluator AI");
            titleLabel.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 15px; -fx-font-weight: bold;");

            VBox splashLayout = new VBox(12, splashIconView, titleLabel);
            splashLayout.setAlignment(Pos.CENTER);
            splashLayout.setStyle("-fx-background-color: #1a202c; -fx-padding: 30;");

            Scene splashScene = new Scene(splashLayout, 320, 320);
            splashStage.setScene(splashScene);
            splashStage.getIcons().add(appIcon);
            splashStage.show();

            Task<InitResult> loadTask = new Task<>() {
                @Override
                protected InitResult call() throws Exception {
                    String fxmlPath = "/fxml/LoginView.fxml";
                    String title = "CodeEvaluator AI - Login";

                    if (SessionManager.hasActiveSession()) {
                        String savedEmail = SessionManager.getSavedSession();
                        User user = userRepository.getUserByEmail(savedEmail);

                        if (user != null) {
                            UserSession.getInstance().login(user);

                            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                                fxmlPath = "/fxml/admin/AdminDashboardView.fxml";
                                title = "CodeEvaluator AI - Admin Dashboard";
                            } else {
                                fxmlPath = "/fxml/student/StudentDashboardView.fxml";
                                title = "CodeEvaluator AI - Student Dashboard";
                            }

                            System.out.println("Persistent Session Restored for: " + user.getEmail() + " (" + user.getRole() + ")");
                        } else {
                            SessionManager.clearSession();
                        }
                    }

                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                    Parent root = loader.load();
                    Scene scene = new Scene(root);

                    return new InitResult(scene, title);
                }
            };

            loadTask.setOnSucceeded(e -> {
                splashStage.close();

                InitResult result = loadTask.getValue();
                primaryStage.getIcons().add(appIcon);
                primaryStage.setTitle(result.title);
                primaryStage.setScene(result.scene);
                primaryStage.setResizable(true);
                primaryStage.show();
            });

            loadTask.setOnFailed(e -> {
                splashStage.close();
                System.err.println("Error initializing application session or view!");
                loadTask.getException().printStackTrace();
            });

            new Thread(loadTask).start();

        } catch (Exception e) {
            System.err.println("Error starting main application!");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}