package org.example.controller;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
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
import org.example.components.problemForm.ProblemFormComponent;
import org.example.components.problemview.ProblemViewModelComponent;
import org.example.model.Category;
import org.example.model.Problem;
import org.example.model.TestCase;
import org.example.service.CategoryService;
import org.example.service.ProblemService;
import org.example.service.TestCaseService;
import org.example.util.ToastUtil;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ProblemController {

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> difficultyDropdown;

    @FXML
    private ComboBox<Category> categoriesDropdown;

    @FXML
    private TableView<Problem> tableView;

    @FXML
    private TableColumn<Problem, String> title;

    @FXML
    private TableColumn<Problem, String> categoryName;

    @FXML
    private TableColumn<Problem, String> difficulty;

    @FXML
    private TableColumn<Problem, String> sampleInput;

    @FXML
    private TableColumn<Problem, Void> problemAction;

    @FXML
    private Pagination pagination;

    private final int ROWS_PER_PAGE = 7;

    private String currentSearch = "";
    private String currentDifficulty = null;
    private Integer currentCategoryId = null;

    private boolean suppressPaginationListener = false;

    private long pageRequestId = 0;
    private long paginationRequestId = 0;

    private final CategoryService categoryService = new CategoryService();
    private final ProblemService problemService = new ProblemService();
    private final TestCaseService testCaseService = new TestCaseService();

    @FXML
    public void initialize() {

        difficultyDropdown.getItems().setAll("ALL", "EASY", "MEDIUM", "HARD");

        title.setCellValueFactory(new PropertyValueFactory<>("title"));

        categoryName.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        difficulty.setCellValueFactory(new PropertyValueFactory<>("difficulty"));

        setupTable();
        setupActionsColumn();
        setupCategoryDropdown();
        setupFilterListeners();

        setupPagination(currentSearch, currentDifficulty, currentCategoryId);

        loadPage(0, currentSearch, currentDifficulty, currentCategoryId);
    }

    private void setupTable() {

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

        tableView.setRowFactory(tv -> new TableRow<Problem>() {

            @Override
            protected void updateItem(Problem item, boolean empty) {

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

        if (sampleInput != null) {

            sampleInput.setCellValueFactory(new PropertyValueFactory<>("sampleInput"));

            sampleInput.setCellFactory(col -> new TableCell<Problem, String>() {

                @Override
                protected void updateItem(String item, boolean empty) {

                    super.updateItem(item, empty);

                    if (empty || item == null) {

                        setText(null);

                    } else {

                        setText(item.replaceAll("\\r?\\n", " "));
                    }
                }
            });
        }
    }

    private void setupFilterListeners() {

        pagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {

            if (suppressPaginationListener) {
                return;
            }

            loadPage(newValue.intValue(), currentSearch, currentDifficulty, currentCategoryId);
        });

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {

            currentSearch = newValue == null ? "" : newValue.trim();

            applyFilters();
        });

        difficultyDropdown.valueProperty().addListener((observable, oldValue, newValue) -> {

            if (newValue == null || "ALL".equals(newValue)) {

                currentDifficulty = null;

            } else {

                currentDifficulty = newValue;
            }

            applyFilters();
        });

        categoriesDropdown.valueProperty().addListener((observable, oldValue, newValue) -> {

            if (newValue == null) {

                currentCategoryId = null;

            } else {

                currentCategoryId = newValue.getId();
            }

            applyFilters();
        });
    }

    private void applyFilters() {

        suppressPaginationListener = true;

        pagination.setCurrentPageIndex(0);

        suppressPaginationListener = false;

        setupPagination(currentSearch, currentDifficulty, currentCategoryId);

        loadPage(0, currentSearch, currentDifficulty, currentCategoryId);
    }

    private void setupCategoryDropdown() {
        categoriesDropdown.setCellFactory(param -> new ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        categoriesDropdown.setButtonCell(new ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Select Category");
                } else {
                    setText(item.getName());
                }
            }
        });

        loadCategories();
    }

    private void loadCategories() {
        Task<List<Category>> task = new Task<>() {
            @Override
            protected List<Category> call() throws Exception {
                return categoryService.getCategoriesNames();
            }
        };

        task.setOnSucceeded(event -> {
            List<Category> categories = task.getValue();
            if (categories != null) {
                Category allCategory = new Category();
                allCategory.setName("ALL");

                categoriesDropdown.getItems().clear();
                categoriesDropdown.getItems().add(allCategory);
                categoriesDropdown.getItems().addAll(categories);
            }
        });

        task.setOnFailed(event -> {
            if (task.getException() != null) {
                task.getException().printStackTrace();
            }
        });

        Thread.ofVirtual().start(task);
    }

    private void loadPage(int pageIndex, String q, String difficulty, Integer categoryId) {

        int page = pageIndex + 1;

        long requestId = ++pageRequestId;

        Task<List<Problem>> task = new Task<>() {

            @Override
            protected List<Problem> call() throws Exception {

                return problemService.getProblems(page, ROWS_PER_PAGE, q, difficulty, categoryId);
            }
        };

        task.setOnSucceeded(event -> {

            if (requestId != pageRequestId) {
                return;
            }

            List<Problem> pageData = task.getValue();

            if (pageData == null) {

                tableView.setItems(FXCollections.observableArrayList());

                return;
            }

            tableView.setItems(FXCollections.observableArrayList(pageData));
        });

        task.setOnFailed(event -> {

            if (requestId != pageRequestId) {
                return;
            }

            if (task.getException() != null) {
                task.getException().printStackTrace();
            }
        });

        Thread.ofVirtual().start(task);
    }

    private void setupPagination(String q, String difficulty, Integer categoryId) {

        long requestId = ++paginationRequestId;

        Task<Integer> task = new Task<>() {

            @Override
            protected Integer call() throws Exception {

                return problemService.getTotalProblemCount(q, difficulty, categoryId);
            }
        };

        task.setOnSucceeded(event -> {

            if (requestId != paginationRequestId) {
                return;
            }

            int totalCount = task.getValue() == null ? 0 : task.getValue();

            int pageCount = (int) Math.ceil((double) totalCount / ROWS_PER_PAGE);

            if (pageCount < 1) {
                pageCount = 1;
            }

            suppressPaginationListener = true;

            pagination.setPageCount(pageCount);

            if (pagination.getCurrentPageIndex() >= pageCount) {

                pagination.setCurrentPageIndex(pageCount - 1);
            }

            suppressPaginationListener = false;
        });

        task.setOnFailed(event -> {

            if (requestId != paginationRequestId) {
                return;
            }

            if (task.getException() != null) {
                task.getException().printStackTrace();
            }
        });

        Thread.ofVirtual().start(task);
    }

    public void refreshPagination() {

        long requestId = ++paginationRequestId;

        int requestedPage = pagination.getCurrentPageIndex();

        Task<Integer> task = new Task<>() {

            @Override
            protected Integer call() throws Exception {

                return problemService.getTotalProblemCount(currentSearch, currentDifficulty, currentCategoryId);
            }
        };

        task.setOnSucceeded(event -> {

            if (requestId != paginationRequestId) {
                return;
            }

            int totalCount = task.getValue() == null ? 0 : task.getValue();

            int pageCount = (int) Math.ceil((double) totalCount / ROWS_PER_PAGE);

            if (pageCount < 1) {
                pageCount = 1;
            }

            int targetPage = Math.min(requestedPage, pageCount - 1);

            suppressPaginationListener = true;

            pagination.setPageCount(pageCount);

            pagination.setCurrentPageIndex(targetPage);

            suppressPaginationListener = false;

            loadPage(targetPage, currentSearch, currentDifficulty, currentCategoryId);
        });

        task.setOnFailed(event -> {

            if (requestId != paginationRequestId) {
                return;
            }

            if (task.getException() != null) {
                task.getException().printStackTrace();
            }
        });

        Thread.ofVirtual().start(task);
    }

    @FXML
    public void openAddProblem(ActionEvent event) {
        ProblemFormComponent formComponent = new ProblemFormComponent();
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(tableView.getScene().getWindow());
        stage.setScene(new Scene(formComponent));
        stage.setResizable(false);
        stage.setTitle("Add Problem");

        try {

            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/app-icon.png"))));

        } catch (Exception ignored) {
        }

        formComponent.setOnCancel(stage::close);

        formComponent.setOnSave((problem, testCasesList) -> {

            Task<Boolean> task = new Task<>() {

                @Override
                protected Boolean call() throws Exception {
                    return problemService.saveProblemWithTestCases(problem, testCasesList);
                }
            };

            task.setOnSucceeded(e -> {
                boolean createdProblem = task.getValue();
                if (createdProblem) {
                    stage.close();
                    refreshPagination();
                    ToastUtil.showSuccess(tableView.getScene().getWindow(), "Problem Added", "New problem has been successfully created.");
                } else {
                    ToastUtil.showError(stage.getScene().getWindow(), "Creation Failed", "Failed to add problem due to a database error.");
                }
            });

            task.setOnFailed(e -> {
                ToastUtil.showError(stage.getScene().getWindow(), "Creation Failed", "Failed to add problem due to a database error.");
                if (task.getException() != null) {
                    task.getException().printStackTrace();
                }
            });

            Thread.ofVirtual().start(task);
        });

        stage.showAndWait();
    }

    public void openViewProblemModel(Problem problem) {

        Task<List<TestCase>> fetchTestCases = new Task<>() {
            @Override
            protected List<TestCase> call() throws Exception {
                return testCaseService.getTestCasesByProblemId(problem.getId());
            }
        };

        fetchTestCases.setOnSucceeded(event -> {

            List<TestCase> testCases = fetchTestCases.getValue();

            ProblemViewModelComponent problemViewModel = new ProblemViewModelComponent();

            problemViewModel.setProblem(problem, testCases);

            Stage stage = new Stage();

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(tableView.getScene().getWindow());

            Scene scene = new Scene(problemViewModel, 700, 560);

            stage.setScene(scene);

            stage.setWidth(700);
            stage.setHeight(560);

            stage.setMinWidth(700);
            stage.setMinHeight(560);

            stage.setMaxWidth(700);
            stage.setMaxHeight(560);

            stage.setResizable(false);

            stage.setTitle("View Problem: " + problem.getTitle());

            try {
                stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/app-icon.png"))));
            } catch (Exception ignored) {
            }

            stage.showAndWait();
        });

        fetchTestCases.setOnFailed(event -> {
            fetchTestCases.getException().printStackTrace();
        });

        Thread.ofVirtual().start(fetchTestCases);
    }

    private void setupActionsColumn() {

        problemAction.setCellFactory(param -> new TableCell<>() {

            private final Button viewBtn = new Button();

            private final Button editBtn = new Button();

            private final Button deleteBtn = new Button();


            private final HBox container = new HBox(8, viewBtn, editBtn, deleteBtn);

            {
                container.setAlignment(Pos.CENTER_LEFT);

                FontIcon viewIcon = new FontIcon(Feather.EYE);

                viewIcon.getStyleClass().add("action-icon-view");

                FontIcon editIcon = new FontIcon(Feather.EDIT_2);

                editIcon.getStyleClass().add("action-icon");

                FontIcon deleteIcon = new FontIcon(Feather.TRASH_2);

                deleteIcon.getStyleClass().add("action-icon-danger");

                viewBtn.setGraphic(viewIcon);

                editBtn.setGraphic(editIcon);

                deleteBtn.setGraphic(deleteIcon);

                viewBtn.getStyleClass().add("icon-btn-view");

                editBtn.getStyleClass().add("icon-btn-edit");

                deleteBtn.getStyleClass().add("icon-btn-delete");

                viewBtn.setOnAction(event -> {
                    Problem problem = getTableView().getItems().get(getIndex());

                    openViewProblemModel(problem);
                });

                editBtn.setOnAction(event -> {

                    Problem problem = getTableView().getItems().get(getIndex());

                    openEditProblemModal(problem);
                });

                deleteBtn.setOnAction(event -> {

                    Problem problem = getTableView().getItems().get(getIndex());

                    openDeleteConfirmationModal(problem);
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

    private void openEditProblemModal(Problem problem) {

        Task<List<TestCase>> fetchTestCases = new Task<>() {
            @Override
            protected List<TestCase> call() throws Exception {
                return testCaseService.getTestCasesByProblemId(problem.getId());
            }
        };

        fetchTestCases.setOnSucceeded(event -> {
            List<TestCase> testCases = fetchTestCases.getValue();

            ProblemFormComponent formComponent = new ProblemFormComponent();
            formComponent.setProblem(problem, testCases);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(tableView.getScene().getWindow());
            stage.setScene(new Scene(formComponent));
            stage.setResizable(false);
            stage.setTitle("Edit Problem: " + problem.getTitle());

            try {
                stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/app-icon.png"))));
            } catch (Exception ignored) {
            }

            formComponent.setOnCancel(stage::close);

            formComponent.setOnSave((updatedData, testCasesList) -> {
                Task<Boolean> task = new Task<>() {
                    @Override
                    protected Boolean call() throws Exception {
                        return problemService.saveProblemWithTestCases(updatedData, testCasesList);
                    }
                };

                task.setOnSucceeded(e -> {
                    boolean updatedProblem = task.getValue();
                    if (updatedProblem) {
                        stage.close();
                        refreshPagination();
                        ToastUtil.showSuccess(
                                tableView.getScene().getWindow(),
                                "Problem Updated",
                                "Problem '" + updatedData.getTitle() + "' has been successfully updated."
                        );
                    } else {
                        ToastUtil.showError(
                                stage.getScene().getWindow(),
                                "Update Failed",
                                "Failed to update problem due to a database error."
                        );
                    }
                });

                task.setOnFailed(e -> {
                    ToastUtil.showError(
                            stage.getScene().getWindow(),
                            "Update Failed",
                            "Failed to update problem due to an unexpected error."
                    );
                    if (task.getException() != null) {
                        task.getException().printStackTrace();
                    }
                });

                Thread.ofVirtual().start(task);
            });

            // Stage show call ko FX callback ke andar hi rakhen
            stage.showAndWait();
        });

        fetchTestCases.setOnFailed(event -> {
            ToastUtil.showError(
                    tableView.getScene().getWindow(),
                    "Fetch Failed",
                    "Failed to load test cases for this problem."
            );
            if (fetchTestCases.getException() != null) {
                fetchTestCases.getException().printStackTrace();
            }
        });

        Thread.ofVirtual().start(fetchTestCases);
    }

    private void openDeleteConfirmationModal(Problem problem) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Problem");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete '" + problem.getTitle() + "'?\nThis action cannot be undone.");
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
            Task<Boolean> deleteProblemTask = getDeleteProblemTask(problem);

            Thread.ofVirtual().start(deleteProblemTask);
        }
    }

    private Task<Boolean> getDeleteProblemTask(Problem problem) {
        Task<Boolean> deleteProblemTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return problemService.deleteProblemById(problem.getId());
            }
        };

        deleteProblemTask.setOnSucceeded(e -> {
            boolean deleted = deleteProblemTask.getValue();

            if (deleted) {
                refreshPagination();
                ToastUtil.showSuccess(tableView.getScene().getWindow(), "Problem Deleted", "Problem '" + problem.getTitle() + "' has been successfully deleted.");
            } else {
                ToastUtil.showError(tableView.getScene().getWindow(), "Deletion Failed", "Failed to delete the problem due to a database error.");
            }
        });

        deleteProblemTask.setOnFailed(e -> {
            ToastUtil.showError(tableView.getScene().getWindow(), "Deletion Failed", "An unexpected error occurred while deleting the problem.");
            if (deleteProblemTask.getException() != null) {
                deleteProblemTask.getException().printStackTrace();
            }
        });
        return deleteProblemTask;
    }
}