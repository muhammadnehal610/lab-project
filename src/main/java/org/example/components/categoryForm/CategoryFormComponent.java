package org.example.components.categoryForm;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.example.model.Category;

import java.io.IOException;
import java.util.function.Consumer;

public class CategoryFormComponent extends VBox {
    @FXML
    private Label formTitleLabel;

    @FXML
    private TextField nameField;

    @FXML
    private  TextField slugField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private Button cancelBtn;

    @FXML
    private Button saveBtn;

    private Category currentCategory;
    private Consumer<Category> onSaveListener;
    private Runnable onCancelListener;

    public  CategoryFormComponent(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/components/CategoryFormModal.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        setupListeners();
    }

    private  void setupListeners(){
        nameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (currentCategory == null && newVal != null) {
                slugField.setText(newVal.toLowerCase().trim()
                        .replaceAll("[^a-z0-9\\s-]", "")
                        .replaceAll("\\s+", "-"));
            }
        });
    }

    public void setCategory(Category category) {
        this.currentCategory = category;
        if (category != null) {
            formTitleLabel.setText("Edit Category");
            saveBtn.setText("Update Category");
            nameField.setText(category.getName());
            slugField.setText(category.getSlug());
            descriptionArea.setText(category.getDescription());
        } else {
            resetForm();
        }
    }

    public void resetForm() {
        this.currentCategory = null;
        formTitleLabel.setText("Add New Category");
        saveBtn.setText("Save Category");
        nameField.clear();
        slugField.clear();
        descriptionArea.clear();
    }

    public void setOnSave(Consumer<Category> onSaveListener) {
        this.onSaveListener = onSaveListener;
    }

    public void setOnCancel(Runnable onCancelListener) {
        this.onCancelListener = onCancelListener;
    }

    @FXML
    private void handleSave() {
        if (nameField.getText().trim().isEmpty() || slugField.getText().trim().isEmpty()) {
            if (nameField.getText().trim().isEmpty()) nameField.setStyle("-fx-border-color: #ef4444;");
            if (slugField.getText().trim().isEmpty()) slugField.setStyle("-fx-border-color: #ef4444;");
            return;
        }

        if (currentCategory == null) {
            currentCategory = new Category();
        }

        currentCategory.setName(nameField.getText().trim());
        currentCategory.setSlug(slugField.getText().trim());
        currentCategory.setDescription(descriptionArea.getText().trim());

        if (onSaveListener != null) {
            onSaveListener.accept(currentCategory);
        }
    }

    @FXML
    private void handleCancel() {
        if (onCancelListener != null) {
            onCancelListener.run();
        }
    }
}
