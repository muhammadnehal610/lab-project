package org.example.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.common.response.StudentProblemData;
import org.example.common.response.StudentProblemResponse;
import org.example.model.Category;
import org.example.model.ProgressStats;
import org.example.model.User;
import org.example.service.CategoryService;
import org.example.service.ProblemService;
import org.example.service.StatsService;
import org.example.util.SceneManager;
import org.example.util.SessionManager;
import org.example.util.UserSession;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public class StudentDashboardController {
    User currentUser = UserSession.getInstance().getCurrentUser();

    @FXML
    private HBox profileHeaderBox;

    @FXML
    private Label userName;

    @FXML
    private Button userDropdownBtn;

    @FXML
    private Label welcomeTxt;

    @FXML
    private HBox categoryContainer;

    @FXML
    private TextField searchField;

    @FXML
    private Label solvedCount;

    @FXML
    private ScrollPane mainScrollPane;

    @FXML
    private VBox problemListContainer;

    @FXML
    private ComboBox<String> difficultyDropdown;


    @FXML private Label totalSolvedLabel;
    @FXML private ProgressBar totalProgressBar;
    @FXML private Label totalPercentageLabel;

    @FXML private Label easySolvedLabel;
    @FXML private ProgressBar easyProgressBar;
    @FXML private Label easyPercentageLabel;

    @FXML private Label medSolvedLabel;
    @FXML private ProgressBar medProgressBar;
    @FXML private Label medPercentageLabel;

    @FXML private Label hardSolvedLabel;
    @FXML private ProgressBar hardProgressBar;
    @FXML private Label hardPercentageLabel;

    private String currentSearch = "";
    private int currentPage = 0;
    private final int PAGE_SIZE = 15;
    private boolean isLoading = false;
    private boolean hasMoreData = true;
    private boolean isUpdatingCategories = false;

    private Integer currentCategoryId = null;
    private String currentDifficulty = null;

    private final ToggleGroup categoryGroup = new ToggleGroup();

    private final CategoryService categoryService = new CategoryService();

    private final ProblemService problemService = new ProblemService();

    private final StatsService statsService = new StatsService();

    @FXML
    public void initialize() {
        setupProfileDropdown();
        displayGreeting();

        loadDashboardProgress();

        difficultyDropdown.getItems().setAll("ALL", "EASY", "MEDIUM", "HARD");

        difficultyDropdown.setOnAction(e -> {
            String selected = difficultyDropdown.getValue();
            currentDifficulty = "All".equalsIgnoreCase(selected) ? null : selected;
            applyFiltersAndReload();
        });

        categoryGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdatingCategories) return;

            if (newVal != null) {
                ToggleButton selectedBtn = (ToggleButton) newVal;
                Object categoryIdData = selectedBtn.getUserData();

                currentCategoryId = (categoryIdData instanceof Integer) ? (Integer) categoryIdData : null;
                applyFiltersAndReload();
            }
        });


        mainScrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() >= 0.9 && !isLoading && hasMoreData) {
                loadMoreProblems();
            }
        });

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            currentSearch = newValue == null ? "" : newValue.trim();
            applyFiltersAndReload();
        });

        loadCategories();

        loadMoreProblems();
    }

    private void loadDashboardProgress() {
        new Thread(() -> {
            ProgressStats stats = statsService.getUserProgress(currentUser.getId());

            Platform.runLater(() -> {
                updateCard(totalSolvedLabel, totalProgressBar, totalPercentageLabel,
                        stats.totalSolved(), stats.totalProblems());

                updateCard(easySolvedLabel, easyProgressBar, easyPercentageLabel,
                        stats.easySolved(), stats.totalEasy());

                updateCard(medSolvedLabel, medProgressBar, medPercentageLabel,
                        stats.mediumSolved(), stats.totalMedium());

                updateCard(hardSolvedLabel, hardProgressBar, hardPercentageLabel,
                        stats.hardSolved(), stats.totalHard());
            });
        }).start();
    }

    private void updateCard(Label ratioLabel, ProgressBar progressBar, Label percentLabel, int solved, int total) {
        ratioLabel.setText(solved + " / " + total);

        double progressValue = (total == 0) ? 0.0 : (double) solved / total;
        progressBar.setProgress(progressValue);

        int percentage = (int) (progressValue * 100);
        percentLabel.setText(percentage + "%");
    }

    private void setupProfileDropdown() {
        ContextMenu dropdownMenu = new ContextMenu();
        MenuItem profileItem = new MenuItem("Profile");
        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.setOnAction(this::handleLogut);
        dropdownMenu.getItems().addAll(profileItem, logoutItem);

        profileHeaderBox.setOnMouseClicked(e -> {
            if (dropdownMenu.isShowing()) {
                dropdownMenu.hide();
            } else {
                dropdownMenu.show(profileHeaderBox, Side.BOTTOM, 0, 0);

                double boxWidth = profileHeaderBox.getBoundsInLocal().getWidth();
                double menuWidth = dropdownMenu.getWidth();

                double dx = boxWidth - menuWidth;

                dropdownMenu.show(profileHeaderBox, Side.BOTTOM, dx, 0);
            }
        });

        userName.setText(currentUser.getFullName());

        FontIcon icon = new FontIcon(Feather.CHEVRON_DOWN);
        icon.getStyleClass().add("icon-dropdown");
        icon.setIconSize(20);
        userDropdownBtn.setGraphic(icon);
        userDropdownBtn.getStyleClass().add("icon-dropdown-btn");
    }

    @FXML
    void handleLogut(ActionEvent event) {
        SessionManager.clearSession();

        UserSession.getInstance().logout();

        SceneManager.redirectToLogin(event);
    }

    private void displayGreeting() {
        String greeting = "Welcome back, " + currentUser.getFirstName() + "!";
        welcomeTxt.setText(greeting);
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
                allCategory.setName("All Topics");

                categories.addFirst(allCategory);

                isUpdatingCategories = true;
                categoryContainer.getChildren().clear();

                for (int i = 0; i < categories.size(); i++) {
                    Category category = categories.get(i);

                    ToggleButton pill = new ToggleButton(category.getName());
                    pill.setToggleGroup(categoryGroup);
                    pill.getStyleClass().add("category-pill");

                    pill.setUserData(category.getId());

                    if (i == 0) {
                        pill.setSelected(true);
                    }

                    categoryContainer.getChildren().add(pill);
                }

                isUpdatingCategories = false;
            }
        });

        task.setOnFailed(event -> {
            isUpdatingCategories = false;
            if (task.getException() != null) {
                task.getException().printStackTrace();
            }
        });

        Thread.ofVirtual().start(task);
    }

    private void loadMoreProblems() {
        if (isLoading || !hasMoreData) return;
        isLoading = true;

        Task<StudentProblemResponse> task = new Task<>() {
            @Override
            protected StudentProblemResponse call() {
                int offset = currentPage * PAGE_SIZE;
                return problemService.getStudentDashboardProblems(
                        currentUser.getId(),
                        PAGE_SIZE,
                        offset,
                        currentCategoryId,
                        currentDifficulty,
                        currentSearch
                );
            }
        };

        task.setOnSucceeded(e -> {
            StudentProblemResponse response = task.getValue();

            List<StudentProblemData> newProblems = response.getData();

            if (newProblems == null || newProblems.isEmpty() || newProblems.size() < PAGE_SIZE) {
                hasMoreData = false;
            }

            if (newProblems != null && !newProblems.isEmpty()) {
                Platform.runLater(() -> {
                    int startingIndex = problemListContainer.getChildren().size();
                    for (int i = 0; i < newProblems.size(); i++) {
                        StudentProblemData problem = newProblems.get(i);

                        solvedCount.setText(response.getTotalSolved() + "/" + response.getTotalRecords() + " Solved");

                        HBox row = createProblemRow(problem, startingIndex + i);
                        problemListContainer.getChildren().add(row);
                    }
                });
                currentPage++;
            }
            isLoading = false;
        });

        task.setOnFailed(e -> {
            isLoading = false;
            task.getException().printStackTrace();
        });

        Thread.ofVirtual().start(task);
    }

    private HBox createProblemRow(StudentProblemData problem, int index) {
        HBox row = new HBox(20.0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12.0, 20.0, 12.0, 20.0));

        row.getStyleClass().add("list-row");
        if (index % 2 == 0) {
            row.getStyleClass().add("list-row-even");
        } else {
            row.getStyleClass().add("list-row-odd");
        }

        FontIcon statusIcon = new FontIcon(Feather.CIRCLE);
        if ("SOLVED".equalsIgnoreCase(problem.getStatus())) {
            statusIcon = new FontIcon(Feather.CHECK_CIRCLE);
            statusIcon.getStyleClass().add("status-icon-solved");
        } else if ("ATTEMPTED".equalsIgnoreCase(problem.getStatus())) {
            statusIcon = new FontIcon(Feather.X_CIRCLE);
            statusIcon.getStyleClass().add("status-icon-attempted");
        }
        statusIcon.getStyleClass().add("status-icon");

        Label titleLabel = new Label(problem.getTitle());
        titleLabel.getStyleClass().add("problem-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Label categoryLabel = new Label(problem.getCategoryName());
        categoryLabel.getStyleClass().add("category-label");
        categoryLabel.setPrefWidth(220.0);
        categoryLabel.setAlignment(Pos.CENTER_LEFT);

        Label difficultyLabel = new Label(problem.getDifficulty().toUpperCase());
        difficultyLabel.getStyleClass().add("difficulty-" + problem.getDifficulty().toLowerCase());
        difficultyLabel.setPrefWidth(80.0);
        difficultyLabel.setAlignment(Pos.CENTER_LEFT);

        Button actionBtn = new Button("SOLVED".equalsIgnoreCase(problem.getStatus()) ? "Review" : "Solve");
        actionBtn.getStyleClass().add("solve-btn");
        actionBtn.setPrefWidth(80.0);

        actionBtn.setOnAction(e -> {
            System.out.println("Opening workspace for problem: " + problem.getTitle());
            openWorkspace(e, problem.getProblemId());
        });

        row.getChildren().addAll(statusIcon, titleLabel, categoryLabel, difficultyLabel, actionBtn);

        return row;
    }

    private void applyFiltersAndReload() {
        currentPage = 0;
        hasMoreData = true;
        problemListContainer.getChildren().clear();
        loadMoreProblems();
    }

    private void openWorkspace(ActionEvent event, int id) {
        SceneManager.switchScene(
                event,
                "/fxml/student/WorkspaceView.fxml",
                "CodeEvaluator - Workspace",
                (WorkspaceController controller) -> {
                    controller.setWorkspaceId(id);
                }
        );
    }
}