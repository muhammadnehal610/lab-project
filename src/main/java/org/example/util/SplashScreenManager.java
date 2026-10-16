package org.example.util;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.util.Objects;

public class SplashScreenManager {
    private final Stage splashStage;

    public SplashScreenManager() {
        splashStage = new Stage();
        splashStage.initStyle(StageStyle.UNDECORATED);

        Image appIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/app-icon.png")));
        ImageView splashIconView = new ImageView(appIcon);
        splashIconView.setFitWidth(110);
        splashIconView.setFitHeight(110);
        splashIconView.setPreserveRatio(true);

        Label titleLabel = new Label("CodeEvaluator");
        titleLabel.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 15px; -fx-font-weight: bold;");

        VBox splashLayout = new VBox(12, splashIconView, titleLabel);
        splashLayout.setAlignment(Pos.CENTER);
        splashLayout.setStyle("-fx-background-color: #1a202c; -fx-padding: 30;");

        Scene splashScene = new Scene(splashLayout, 320, 320);
        splashStage.setScene(splashScene);
        splashStage.getIcons().add(appIcon);
    }

    public void show() {
        splashStage.show();
    }

    public void close() {
        splashStage.close();
    }
}