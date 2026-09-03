package org.example.components.logo;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class LogoComponent extends HBox {
    @FXML
    private ImageView logoImage;
    @FXML
    private Label logoText;

    public LogoComponent() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/components/LogoView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void setTitle(String text) {
        logoText.setText(text);
    }

    public void setLogoSize(double width, double height) {
        logoImage.setFitWidth(width);
        logoImage.setFitHeight(height);
    }
}