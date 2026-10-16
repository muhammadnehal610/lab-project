package org.example.controller;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
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
import org.example.components.categoryForm.CategoryFormComponent;
import org.example.model.Category;
import org.example.service.CategoryService;
import org.example.util.ToastUtil;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CategoryController {
    @FXML
    private TextField searchField;

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

    @FXML
    private Pagination pagination;

    private final int ROWS_PER_PAGE = 7;
    private String currentSearch = "";

    private FilteredList<Category> filteredCategoryList;
    private final ObservableList<Category> masterCategoryList = FXCollections.observableArrayList();

    private final CategoryService categoryService = new CategoryService();

    @FXML
    public void initialize() {
        setupTableColumns();
        setupTableProperties();

        filteredCategoryList = new FilteredList<>(masterCategoryList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String query = newValue == null ? "" : newValue.trim().toLowerCase();
            filteredCategoryList.setPredicate(category -> {
                if (query.isEmpty()) return true;
                return category.getName().toLowerCase().contains(query) ||
                        (category.getSlug() != null && category.getSlug().toLowerCase().contains(query));
            });
            recalculatePaginationAndRender();
        });

        pagination.currentPageIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            updateTableForCurrentPage(newIdx.intValue());
        });

        loadAllCategoriesFromDB();
    }

    private void setupTableColumns() {
        categoryName.setCellValueFactory(new PropertyValueFactory<>("name"));

        if (categorySlug != null) {
            categorySlug.setCellValueFactory(new PropertyValueFactory<>("slug"));
        }

        if (categoryDescription != null) {
            categoryDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        }

        setupActionsColumn();
    }

    private void setupTableProperties() {

        tableView.setFixedCellSize(46);

        var calculatedHeight = Bindings.createDoubleBinding(() -> {

            int size = tableView.getItems() != null ? tableView.getItems().size() : 0;

            int visibleRows = Math.min(size, ROWS_PER_PAGE);

            double rowHeight = 46.0;
            double headerHeight = 46.0;
            double extraSpace = 4.0;

            return (visibleRows * rowHeight) + headerHeight + extraSpace;

        }, tableView.itemsProperty());

        tableView.prefHeightProperty().bind(calculatedHeight);
        tableView.minHeightProperty().bind(calculatedHeight);
        tableView.maxHeightProperty().bind(calculatedHeight);

        tableView.setRowFactory(tv -> new TableRow<Category>() {

            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setStyle("-fx-border-color: transparent;" + "-fx-border-width: 0;");

                } else if (getIndex() == tv.getItems().size() - 1) {

                    setStyle("-fx-border-color: transparent;" + "-fx-border-width: 0;");

                } else {

                    setStyle("-fx-border-color: -fx-border-default;" + "-fx-border-width: 0 0 1px 0;");
                }
            }
        });
    }

    private void loadAllCategoriesFromDB() {
        Task<List<Category>> fetchTask = new Task<>() {
            @Override
            protected List<Category> call() throws Exception {
                return categoryService.getCategories("");
            }
        };

        fetchTask.setOnSucceeded(e -> {
            masterCategoryList.setAll(fetchTask.getValue());
            recalculatePaginationAndRender();
        });

        Thread.ofVirtual().start(fetchTask);
    }

    private void recalculatePaginationAndRender() {
        int totalItems = filteredCategoryList.size();
        int pageCount = (int) Math.ceil((double) totalItems / ROWS_PER_PAGE);
        pagination.setPageCount(pageCount == 0 ? 1 : pageCount);

        if (pagination.getCurrentPageIndex() >= pageCount) {
            pagination.setCurrentPageIndex(Math.max(0, pageCount - 1));
        } else {
            updateTableForCurrentPage(pagination.getCurrentPageIndex());
        }
    }

    private void updateTableForCurrentPage(int pageIndex) {
        if (filteredCategoryList.isEmpty()) {
            tableView.setItems(FXCollections.emptyObservableList());
            return;
        }

        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filteredCategoryList.size());

        if (fromIndex < filteredCategoryList.size()) {
            List<Category> pageSubList = filteredCategoryList.subList(fromIndex, toIndex);
            tableView.setItems(FXCollections.observableArrayList(pageSubList));
        } else {
            tableView.setItems(FXCollections.emptyObservableList());
        }
    }

    public void refreshPagination() {
        loadAllCategoriesFromDB();
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

        try {
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/app-icon.png"))));
        } catch (Exception ignored) {
        }

        formComponent.setOnCancel(stage::close);

        formComponent.setOnSave(newCategoryData -> {

            Task<Category> createCategoryTask = new Task<>() {
                @Override
                protected Category call() throws Exception {
                    return categoryService.addCategory(newCategoryData);
                }
            };

            createCategoryTask.setOnSucceeded(e -> {
                Category createdCategory = createCategoryTask.getValue();

                if (createdCategory != null) {
                    stage.close();
                    refreshPagination();
                    ToastUtil.showSuccess(tableView.getScene().getWindow(), "Category Added", "Category '" + createdCategory.getName() + "' has been successfully created.");
                } else {
                    ToastUtil.showError(stage.getScene().getWindow(), "Creation Failed", "Failed to add category due to a database error.");
                }
            });

            createCategoryTask.setOnFailed(e -> {
                ToastUtil.showError(stage.getScene().getWindow(), "Creation Failed", "An unexpected error occurred while adding the category.");
                if (createCategoryTask.getException() != null) {
                    createCategoryTask.getException().printStackTrace();
                }
            });

            Thread.ofVirtual().start(createCategoryTask);
        });

        stage.showAndWait();
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
                    openEditCategoryModal(category);
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

    private void openEditCategoryModal(Category category) {
        CategoryFormComponent formComponent = new CategoryFormComponent();
        formComponent.setCategory(category);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(tableView.getScene().getWindow());
        stage.setScene(new Scene(formComponent));
        stage.setResizable(false);

        stage.setTitle("Edit Category: " + category.getName());

        try {
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/app-icon.png"))));
        } catch (Exception ignored) {
        }

        formComponent.setOnCancel(stage::close);

        formComponent.setOnSave(updatedData -> {
            Task<Category> updatedCategoryTask = new Task<Category>() {
                @Override
                protected Category call() throws Exception {
                    return categoryService.updateCategory(updatedData);
                }
            };

            updatedCategoryTask.setOnSucceeded(e -> {
                Category updatedCategory = updatedCategoryTask.getValue();

                if (updatedCategory != null) {
                    stage.close();
                    refreshPagination();
                    ToastUtil.showSuccess(tableView.getScene().getWindow(), "Category Updated", "Category '" + updatedCategory.getName() + "' details have been successfully updated.");
                } else {
                    ToastUtil.showError(stage.getScene().getWindow(), "Update Failed", "Failed to save changes due to a database error.");
                }
            });

            updatedCategoryTask.setOnFailed(e -> {
                ToastUtil.showError(stage.getScene().getWindow(), "Update Failed", "An unexpected error occurred while saving changes.");
                if (updatedCategoryTask.getException() != null) {
                    updatedCategoryTask.getException().printStackTrace();
                }
            });

            Thread.ofVirtual().start(updatedCategoryTask);

        });

        stage.showAndWait();
    }

    private void openDeleteConfirmationModal(Category category) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Category");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete '" + category.getName() + "'?\nThis action cannot be undone.");
        alert.initOwner(tableView.getScene().getWindow());

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        dialogPane.getStyleClass().add("custom-alert");

        alert.setGraphic(null);

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);

        okButton.setText("Delete");
        okButton.getStyleClass().addAll("alert-btn", "alert-btn-danger");
        cancelButton.getStyleClass().addAll("alert-btn", "alert-btn-secondary");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Boolean> deletedCategoryTask = getDeletedCategoryTask(category);

            Thread.ofVirtual().start(deletedCategoryTask);
        }
    }

    private Task<Boolean> getDeletedCategoryTask(Category category) {
        Task<Boolean> deletedCategoryTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return categoryService.deleteCategory(category.getId());
            }
        };

        deletedCategoryTask.setOnSucceeded(e -> {
            boolean deleted = deletedCategoryTask.getValue();

            if (deleted) {
                refreshPagination();
                ToastUtil.showSuccess(tableView.getScene().getWindow(), "Category Deleted", "Category '" + category.getName() + "' has been successfully deleted.");
            }
        });
        return deletedCategoryTask;
    }
}