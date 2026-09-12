package org.example.util;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

public class ToastUtil {

    private static final String SUCCESS_ICON_PATH = "M22 11.08V12a10 10 0 1 1-5.93-9.14 M22 4L12 14.01l-3-3";
    private static final String ERROR_ICON_PATH = "M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z M12 9v4 M12 17h.01";

    private ToastUtil() {}

    public static void showSuccess(Window owner, String title, String message) {
        show(owner, title, message, "#022c22", "#059669", "#34d399", SUCCESS_ICON_PATH);
    }

    public static void showError(Window owner, String title, String message) {
        show(owner, title, message, "#450a0a", "#dc2626", "#fca5a5", ERROR_ICON_PATH);
    }

    private static void show(Window owner, String titleText, String messageText,
                             String bgColor, String borderColor, String accentColor,
                             String svgPathData) {
        if (owner == null) return;

        Popup popup = new Popup();

        SVGPath icon = new SVGPath();
        icon.setContent(svgPathData);
        icon.setStyle("-fx-fill: transparent; -fx-stroke: " + accentColor + "; -fx-stroke-width: 2; -fx-stroke-line-cap: round; -fx-stroke-line-join: round;");

        Label title = new Label(titleText);
        title.setStyle("-fx-text-fill: " + accentColor + "; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label message = new Label(messageText);
        message.setWrapText(true);
        message.setMaxWidth(260);
        message.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");

        VBox textContainer = new VBox(2, title, message);
        HBox root = new HBox(12, icon, textContainer);
        root.setAlignment(Pos.CENTER_LEFT);
        root.setMaxWidth(320);
        root.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-border-color: " + borderColor + ";" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-padding: 12px 16px;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.5), 10, 0, 0, 4);"
        );

        popup.getContent().add(root);
        popup.setAutoHide(true);

        root.setOpacity(0);
        root.setTranslateY(10);

        double x = owner.getX() ;
        double y = owner.getY() + owner.getHeight() - 90;
        popup.show(owner, x, y);

        Timeline fadeIn = new Timeline(
                new KeyFrame(Duration.millis(180),
                        new KeyValue(root.opacityProperty(), 1.0),
                        new KeyValue(root.translateYProperty(), 0)
                )
        );

        PauseTransition delay = new PauseTransition(Duration.seconds(4));

        Timeline fadeOut = new Timeline(
                new KeyFrame(Duration.millis(180),
                        new KeyValue(root.opacityProperty(), 0.0),
                        new KeyValue(root.translateYProperty(), 10)
                )
        );

        fadeIn.setOnFinished(e -> delay.play());
        delay.setOnFinished(e -> fadeOut.play());
        fadeOut.setOnFinished(e -> popup.hide());

        fadeIn.play();
    }
}