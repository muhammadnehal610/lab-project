package org.example.components.problemForm;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import org.example.model.Category;
import org.example.model.Problem;
import org.example.model.TestCase;
import org.example.service.CategoryService;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

public class ProblemFormComponent extends VBox implements Initializable {

    @FXML
    private Label formTitleLabel;

    @FXML
    private TextField txtTitle;

    @FXML
    private ComboBox<String> difficultyDropdown;

    @FXML
    private ComboBox<Category> categoriesDropdown;

    @FXML
    private HTMLEditor descriptionEditor;

    @FXML
    private TextArea sampleInputTextArea;

    @FXML
    private TextArea sampleOutputTextArea;

    @FXML
    private VBox testCasesContainer;

    @FXML
    private Button cancelBtn;

    @FXML
    private Button saveBtn;

    private Problem currentProblem;
    private BiConsumer<Problem, List<TestCase>> onSaveListener;
    private Runnable onCancelListener;

    private final CategoryService categoryService = new CategoryService();

    public ProblemFormComponent() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/components/ProblemFormModal.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        difficultyDropdown.getItems().setAll("EASY", "MEDIUM", "HARD");

        setupCategoryDropdown();

        if (descriptionEditor != null) {
            descriptionEditor.setHtmlText("<p></p>");
        }

        Platform.runLater(() -> {
            PauseTransition delay = new PauseTransition(Duration.millis(150));
            delay.setOnFinished(e -> {
                enableEditorScrolling();
                customizeEditorToolbar();
                applyEditorThemeAndPlaceholder();
            });
            delay.play();
        });

        resetForm();
    }

    public void setOnSave(BiConsumer<Problem, List<TestCase>> onSaveListener) {
        this.onSaveListener = onSaveListener;
    }

    public void setOnCancel(Runnable onCancelListener) {
        this.onCancelListener = onCancelListener;
    }

    public void setProblem(Problem problem, List<TestCase> existingTestCases) {
        this.currentProblem = problem;
        testCasesContainer.getChildren().clear();

        if (problem != null) {
            formTitleLabel.setText("Edit Problem");
            saveBtn.setText("Update Problem");

            txtTitle.setText(problem.getTitle());
            difficultyDropdown.setValue(problem.getDifficulty());
            descriptionEditor.setHtmlText(problem.getDescription());
            sampleInputTextArea.setText(problem.getSampleInput());
            sampleOutputTextArea.setText(problem.getSampleOutput());

            if (problem.getCategoryId() > 0 && problem.getCategoryName() != null) {
                boolean categoryFound = false;
                for (Category cat : categoriesDropdown.getItems()) {
                    if (cat.getId() == problem.getCategoryId()) {
                        categoriesDropdown.setValue(cat);
                        categoryFound = true;
                        break;
                    }
                }

                if (!categoryFound) {
                    Category preSelectedCategory = new Category();
                    preSelectedCategory.setId(problem.getCategoryId());
                    preSelectedCategory.setName(problem.getCategoryName());

                    categoriesDropdown.getItems().add(0, preSelectedCategory);
                    categoriesDropdown.setValue(preSelectedCategory);
                }
            }

            if (existingTestCases != null && !existingTestCases.isEmpty()) {
                for (int i = 0; i < existingTestCases.size(); i++) {
                    TestCase tc = existingTestCases.get(i);
                    createAndAddTestCaseRow("Test Case " + (i + 1), tc.getInputData(), tc.getExpectedOutput());
                }
            } else {
                handleAddTestCase();
            }
        } else {
            resetForm();
        }
    }

    public void resetForm() {
        this.currentProblem = null;
        formTitleLabel.setText("Add New Problem");
        saveBtn.setText("Save Problem");

        txtTitle.clear();
        difficultyDropdown.getSelectionModel().clearSelection();
        categoriesDropdown.getSelectionModel().clearSelection();
        descriptionEditor.setHtmlText("<p></p>");
        sampleInputTextArea.clear();
        sampleOutputTextArea.clear();

        testCasesContainer.getChildren().clear();
        handleAddTestCase();

        txtTitle.setStyle("");
        difficultyDropdown.setStyle("");
        categoriesDropdown.setStyle("");
    }

    @FXML
    private void handleSave() {
        boolean isValid = true;

        if (txtTitle.getText().trim().isEmpty()) {
            txtTitle.setStyle("-fx-border-color: #ef4444;");
            isValid = false;
        } else {
            txtTitle.setStyle("");
        }

        if (categoriesDropdown.getValue() == null) {
            categoriesDropdown.setStyle("-fx-border-color: #ef4444;");
            isValid = false;
        } else {
            categoriesDropdown.setStyle("");
        }

        if (difficultyDropdown.getValue() == null || difficultyDropdown.getValue().equals("ALL")) {
            difficultyDropdown.setStyle("-fx-border-color: #ef4444;");
            isValid = false;
        } else {
            difficultyDropdown.setStyle("");
        }

        if (!isValid) return;

        if (currentProblem == null) {
            currentProblem = new Problem();
        }

        currentProblem.setTitle(txtTitle.getText().trim());
        currentProblem.setCategoryId(categoriesDropdown.getValue().getId());
        currentProblem.setCategoryName(categoriesDropdown.getValue().getName());
        currentProblem.setDifficulty(difficultyDropdown.getValue());
        currentProblem.setDescription(descriptionEditor.getHtmlText());
        currentProblem.setSampleInput(sampleInputTextArea.getText().trim());
        currentProblem.setSampleOutput(sampleOutputTextArea.getText().trim());

        List<TestCase> extractedTestCases = new ArrayList<>();

        for (Node node : testCasesContainer.getChildren()) {
            if (node instanceof VBox testCaseCard) {
                HBox row = (HBox) testCaseCard.getChildren().get(1);
                VBox inputBox = (VBox) row.getChildren().get(0);
                VBox outputBox = (VBox) row.getChildren().get(1);

                TextArea inputArea = (TextArea) inputBox.getChildren().get(1);
                TextArea outputArea = (TextArea) outputBox.getChildren().get(1);

                String input = inputArea.getText().trim();
                String output = outputArea.getText().trim();

                if (!input.isEmpty() || !output.isEmpty()) {
                    TestCase tc = new TestCase();
                    tc.setInputData(input);
                    tc.setExpectedOutput(output);
                    extractedTestCases.add(tc);
                }
            }
        }

        if (onSaveListener != null) {
            onSaveListener.accept(currentProblem, extractedTestCases);
        }
    }

    @FXML
    private void handleCancel() {
        if (onCancelListener != null) {
            onCancelListener.run();
        }
    }

    @FXML
    private void handleAddTestCase() {
        int caseNumber = testCasesContainer.getChildren().size() + 1;
        createAndAddTestCaseRow("Test Case " + caseNumber, "", "");
    }

    private void createAndAddTestCaseRow(String titleText, String inputData, String outputData) {
        Label title = new Label(titleText);
        title.getStyleClass().add("test-case-heading");

        Button deleteBtn = new Button("×");
        deleteBtn.getStyleClass().add("delete-case-btn");

        HBox cardHeader = new HBox(10, title);
        cardHeader.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        cardHeader.getChildren().addAll(spacer, deleteBtn);

        Label inputLabel = new Label("Input");
        inputLabel.getStyleClass().add("problem-field-label");

        TextArea inputArea = new TextArea(inputData);
        inputArea.setPromptText("Enter test input...");
        inputArea.setWrapText(true);
        inputArea.setPrefRowCount(3);
        inputArea.setPrefHeight(80);
        inputArea.setMinHeight(80);
        inputArea.getStyleClass().add("form-input-area");

        VBox inputBox = new VBox(5, inputLabel, inputArea);
        HBox.setHgrow(inputBox, Priority.ALWAYS);

        Label outputLabel = new Label("Expected Output");
        outputLabel.getStyleClass().add("problem-field-label");

        TextArea outputArea = new TextArea(outputData);
        outputArea.setPromptText("Enter expected output...");
        outputArea.setWrapText(true);
        outputArea.setPrefRowCount(3);
        outputArea.setPrefHeight(80);
        outputArea.setMinHeight(80);
        outputArea.getStyleClass().add("form-input-area");

        VBox outputBox = new VBox(5, outputLabel, outputArea);
        HBox.setHgrow(outputBox, Priority.ALWAYS);

        HBox row = new HBox(16, inputBox, outputBox);

        VBox testCaseCard = new VBox(8, cardHeader, row);
        testCaseCard.getStyleClass().add("test-case-box");

        deleteBtn.setOnAction(e -> {
            testCasesContainer.getChildren().remove(testCaseCard);
            reindexTestCaseTitles();
        });

        testCasesContainer.getChildren().add(testCaseCard);
    }

    private void reindexTestCaseTitles() {
        for (int i = 0; i < testCasesContainer.getChildren().size(); i++) {
            Node node = testCasesContainer.getChildren().get(i);
            if (node instanceof VBox testCaseCard) {
                if (testCaseCard.getChildren().getFirst() instanceof HBox cardHeader) {
                    if (cardHeader.getChildren().getFirst() instanceof Label titleLabel) {
                        titleLabel.setText("Test Case " + (i + 1));
                    }
                }
            }
        }
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
                Category selectedBefore = categoriesDropdown.getValue();

                categoriesDropdown.getItems().clear();
                categoriesDropdown.getItems().addAll(categories);

                if (selectedBefore != null) {
                    for (Category cat : categoriesDropdown.getItems()) {
                        if (Objects.equals(cat.getId(), selectedBefore.getId())) {
                            categoriesDropdown.setValue(cat);
                            break;
                        }
                    }
                }
            }
        });

        task.setOnFailed(event -> {
            if (task.getException() != null) {
                task.getException().printStackTrace();
            }
        });

        Thread.ofVirtual().start(task);
    }

    private void enableEditorScrolling() {
        Node node = descriptionEditor.lookup(".web-view");
        if (node instanceof WebView webView) {
            webView.setMinHeight(180);
            webView.setPrefHeight(200);
            webView.setFocusTraversable(true);
        }
    }

    private void customizeEditorToolbar() {
        for (Node child : descriptionEditor.lookupAll(".tool-bar")) {
            if (child instanceof ToolBar toolBar) {
                toolBar.getItems().removeIf(item -> {
                    if (item instanceof ComboBox) {
                        return true;
                    }
                    if (item instanceof ColorPicker) {
                        return true;
                    }
                    return item.getStyleClass().contains("html-editor-foreground") || item.getStyleClass().contains("html-editor-background");
                });
            }
        }
    }

    private void applyEditorThemeAndPlaceholder() {
        Node node = descriptionEditor.lookup(".web-view");
        if (node instanceof WebView webView) {
            WebEngine engine = webView.getEngine();

            Runnable injectStyleAndPlaceholder = () -> {
                String jsScript = """
                            (function() {
                                var doc = document;
                                var body = doc.body;
                                if (!body) return;
                        
                                // Dark Theme Colors Injection
                                body.style.backgroundColor = '#393E46';
                                body.style.color = '#EEEEEE';
                                body.style.fontFamily = "'Inter', sans-serif";
                                body.style.fontSize = '14px';
                        
                                var placeholder = '%s';
                        
                                function checkPlaceholder() {
                                    if (body.innerText.trim() === '' || body.innerHTML === '<p></p>') {
                                        body.setAttribute('data-placeholder', placeholder);
                                    } else {
                                        body.removeAttribute('data-placeholder');
                                    }
                                }
                        
                                // Style duplicate hone se bachane ke liye ID check
                                if (!doc.getElementById('custom-editor-style')) {
                                    var style = doc.createElement('style');
                                    style.id = 'custom-editor-style';
                                    style.innerHTML = `
                                        body[data-placeholder]:before {
                                            content: attr(data-placeholder);
                                            color: #8C96A3;
                                            position: absolute;
                                            pointer-events: none;
                                        }
                                        a { color: #0284c7; }
                                        code { background: #2B313A; color: #38bdf8; padding: 2px 4px; border-radius: 4px; }
                                    `;
                                    doc.head.appendChild(style);
                                }
                        
                                body.addEventListener('input', checkPlaceholder);
                                body.addEventListener('blur', checkPlaceholder);
                                checkPlaceholder();
                            })();
                        """.formatted("Enter problem description here...");

                try {
                    engine.executeScript(jsScript);
                } catch (Exception ignored) {
                }
            };

            engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    injectStyleAndPlaceholder.run();
                }
            });

            if (engine.getLoadWorker().getState() == javafx.concurrent.Worker.State.SUCCEEDED) {
                injectStyleAndPlaceholder.run();
            }
        }
    }

    public String getDescriptionText() {
        if (descriptionEditor != null) {
            return descriptionEditor.getHtmlText();
        }
        return "";
    }
}