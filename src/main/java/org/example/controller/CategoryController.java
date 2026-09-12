package org.example.controller;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.Notifications;
import org.example.components.categoryForm.CategoryFormComponent;
import org.example.model.Category;
import org.example.service.CategoryService;
import org.example.util.ToastUtil;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.util.Duration;

import java.util.List;
import java.util.Optional;

public class CategoryController {
    @FXML
    private Button openAddCategory;

    @FXML
    private TableView<Category> tableView;

    @FXML
    private TableColumn<Category, String> categoryName;

    @FXML
    private TableColumn<Category, String> categorySlug;

    @FXML
    private TableColumn<Category, String> categoryDescription;

    @FXML
    private TableColumn<Category, Void> categoryAction;

    private final CategoryService categoryService = new CategoryService();

    @FXML
    public void initialize() {
        categoryName.setCellValueFactory(new PropertyValueFactory<>("name"));

        if (categorySlug != null) {
            categorySlug.setCellValueFactory(new PropertyValueFactory<>("slug"));
        }

        if (categoryDescription != null) {
            categoryDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        }

        loadCategoryData();

        tableView.setFixedCellSize(50);

        var calculatedHeight = Bindings.createDoubleBinding(() -> {
            int size = tableView.getItems() != null ? tableView.getItems().size() : 0;
            int visibleRows = Math.min(size, 7);
            return (visibleRows * 50.0) + 50.0;
        }, tableView.itemsProperty(), tableView.getItems());

        tableView.prefHeightProperty().bind(calculatedHeight);
        tableView.minHeightProperty().bind(calculatedHeight);
        tableView.maxHeightProperty().bind(calculatedHeight);

        tableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setStyle("-fx-border-color: transparent;");
                } else if (getIndex() == tv.getItems().size() - 1) {
                    setStyle("-fx-border-color: transparent;");
                } else {
                    setStyle("-fx-border-color: -fx-border-default; -fx-border-width: 0 0 1px 0;");
                }
            }
        });

        setupActionsColumn();
    }

    @FXML
    public void openAddCategory(ActionEvent event) {
        CategoryFormComponent formComponent = new CategoryFormComponent();

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(tableView.getScene().getWindow());
        stage.setScene(new Scene(formComponent));
        stage.setResizable(false);

        stage.setTitle("Add Category");

        formComponent.setOnCancel(stage::close);

        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app-icon.png")));
        } catch (Exception e) {
        }

        stage.showAndWait();
    }

    private void loadCategoryData() {
        List<Category> categories = categoryService.getCategories(1, 10);

        if (categories != null) {
            ObservableList<Category> categoryList = FXCollections.observableArrayList(categories);
            tableView.setItems(categoryList);
        } else {
            System.err.println("No categories found or SQLException occurred.");
        }
    }

    private void setupActionsColumn() {
        categoryAction.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button();
            private final Button deleteBtn = new Button();
            private final HBox container = new HBox(8, editBtn, deleteBtn);

            {
                container.setAlignment(Pos.CENTER_LEFT);

                FontIcon editIcon = new FontIcon(Feather.EDIT_2);
                editIcon.getStyleClass().add("action-icon");

                FontIcon deleteIcon = new FontIcon(Feather.TRASH_2);
                deleteIcon.getStyleClass().add("action-icon-danger");

                editBtn.setGraphic(editIcon);
                deleteBtn.setGraphic(deleteIcon);

                editBtn.getStyleClass().add("icon-btn-edit");
                deleteBtn.getStyleClass().add("icon-btn-delete");

                editBtn.setOnAction(event -> {
                    Category category = getTableView().getItems().get(getIndex());
                    openCategoryModal(category);
                });

                deleteBtn.setOnAction(event -> {
                    Category category = getTableView().getItems().get(getIndex());
                    openDeleteConfirmationModal(category);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    private void openDeleteConfirmationModal(Category category) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Category");

        alert.setHeaderText(null);

        alert.setContentText("Are you sure you want to delete '" + category.getName() + "'?\nThis action cannot be undone.");

        alert.initOwner(tableView.getScene().getWindow());

        DialogPane dialogPane = alert.getDialogPane();

        dialogPane.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-alert");

        alert.setGraphic(null);

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);

        okButton.setText("Delete");
        okButton.getStyleClass().addAll("alert-btn", "alert-btn-danger");
        cancelButton.getStyleClass().addAll("alert-btn", "alert-btn-secondary");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
//            boolean deleted = categoryService.deleteCategory(category.getId());
//            if (deleted) {
            tableView.getItems().remove(category);
            ToastUtil.showSuccess(tableView.getScene().getWindow(), "Category Deleted", "'" + category.getName() + "' successfully remove ho gayi hai.");
//            }
        }
    }

    private void openCategoryModal(Category category) {
        CategoryFormComponent formComponent = new CategoryFormComponent();
        formComponent.setCategory(category);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(tableView.getScene().getWindow());
        stage.setScene(new Scene(formComponent));
        stage.setResizable(false);

        stage.setTitle("Edit Category: " + category.getName());

        formComponent.setOnCancel(stage::close);

        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app-icon.png")));
        } catch (Exception e) {
        }

        stage.showAndWait();
    }


}