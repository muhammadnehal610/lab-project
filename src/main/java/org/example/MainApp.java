package org.example;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.util.DatabaseConnection;
import org.example.util.SessionManager;
import org.example.util.UserSession;
import org.example.util.SplashScreenManager; // Nayi class import ki

import java.util.Objects;

public class MainApp extends Application {
    private final UserRepository userRepository = new UserRepository();

    private static class InitResult {
        private final Scene scene;
        private final String title;

        InitResult(Scene scene, String title) {
            this.scene = scene;
            this.title = title;
        }

        public Scene getScene() { return scene; }
        public String getTitle() { return title; }
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            DatabaseConnection.initializeDatabase();
            loadFonts();

            SplashScreenManager splashScreen = new SplashScreenManager();
            splashScreen.show();

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
                    return new InitResult(new Scene(root), title);
                }
            };

            loadTask.setOnSucceeded(e -> {
                splashScreen.close();
                InitResult result = loadTask.getValue();

                primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/app-icon.png"))));

                // Getters use kar rahe hain (Encapsulation)
                primaryStage.setTitle(result.getTitle());
                primaryStage.setScene(result.getScene());
                primaryStage.setResizable(true);
                primaryStage.show();
            });

            loadTask.setOnFailed(e -> {
                splashScreen.close();
                System.err.println("Error initializing application session or view!");
                loadTask.getException().printStackTrace();
            });

            new Thread(loadTask).start();

        } catch (Exception e) {
            System.err.println("Error starting main application!");
            e.printStackTrace();
        }
    }

    private void loadFonts() {
        Font.loadFont(getClass().getResourceAsStream("/fonts/Inter_18pt-Regular.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Inter_18pt-Medium.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Inter_18pt-SemiBold.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Inter_18pt-Bold.ttf"), 14);
    }

    public static void main(String[] args) {
        launch(args);
    }
}