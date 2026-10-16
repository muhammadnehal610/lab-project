package org.example.controller;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebView;
import org.example.model.Problem;
import org.example.model.ProgressStats;
import org.example.model.TestCase;
import org.example.model.User;
import org.example.service.*;
import org.example.util.SceneManager;
import org.example.util.UserSession;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class WorkspaceController {
    User currentUser = UserSession.getInstance().getCurrentUser();

    private int workspaceId;
    private Problem problem;
    private final ProblemService problemService = new ProblemService();

    @FXML
    private Label problemName;

    @FXML
    private Label formTitleLabel;

    @FXML
    private Label titleLabel;

    @FXML
    private Label categoryLabel;

    @FXML
    private Label difficultyLabel;

    @FXML
    private WebView descriptionWebView;

    @FXML
    private Label sampleInputLabel;

    @FXML
    private Label sampleOutputLabel;

    @FXML
    private WebView codeEditorWebView;

    @FXML
    private ComboBox<String> languageComboBox;

    @FXML
    private ScrollPane terminalScrollPane;

    @FXML
    private TextFlow terminalOutputFlow;

    private final TestCaseService testCaseService = new TestCaseService();
    private final SubmissionService submissionService = new SubmissionService();
    private final CodeExecutionService codeExecutionService = new CodeExecutionService();

    @FXML
    public void initialize() {
        loadCodeEditor();
        setupLanguageComboBox();

        terminalOutputFlow.heightProperty().addListener((observable, oldValue, newValue) -> {
            terminalScrollPane.setVvalue(1.0);
        });


    }






    private void setupLanguageComboBox() {
        languageComboBox.getItems().addAll("Java", "C++");
        languageComboBox.setValue("Java");

        languageComboBox.setOnAction(event -> {
            String selectedLang = languageComboBox.getValue();
            if (selectedLang != null) {
                if (selectedLang.equals("C++")) {
                    codeEditorWebView.getEngine().executeScript("changeLanguage('cpp')");
                } else if (selectedLang.equals("Java")) {
                    codeEditorWebView.getEngine().executeScript("changeLanguage('java')");
                }
            }
        });
    }

    private void loadCodeEditor() {
        URL url = getClass().getResource("/html/editor.html");
        if (url != null) {
            codeEditorWebView.getEngine().load(url.toExternalForm());
        } else {
            System.err.println("Error: editor.html file src/main/resources/ mein nahi mili!");
        }
    }

    @FXML
    public void runCode(ActionEvent event) {
        try {
            clearTerminal();
            appendToTerminal("Fetching code...", "#CCCCCC");

            String userCode = (String) codeEditorWebView.getEngine().executeScript("getCode()");
            String selectedLang = languageComboBox.getValue();
            List<TestCase> testCases = this.testCaseService.getTestCasesByProblemId(problem.getId());

            appendToTerminal("Language Selected: " + selectedLang, "#CCCCCC");
            appendToTerminal("Starting execution in background...\n-----------------------------------", "#888888");

            new Thread(() -> {
                String finalVerdict = codeExecutionService.executeCode(
                        userCode,
                        selectedLang,
                        testCases,
                        this::appendToTerminal
                );

                submissionService.submitCode(
                        currentUser.getId(),
                        problem.getId(),
                        selectedLang,
                        userCode,
                        finalVerdict
                );

                Platform.runLater(() -> {
                    if (finalVerdict.equals("PASSED")) {
                        appendToTerminal("\n✅ Success: All Test Cases Passed!", "#00FF00");
                    } else {
                        appendToTerminal("\n❌ Verdict: " + finalVerdict, "#FF3333");
                    }
                });

            }).start();

        } catch (Exception e) {
            appendToTerminal("System Error: " + e.getMessage(), "#FF3333");
        }
    }

    private void clearTerminal() {
        Platform.runLater(() -> terminalOutputFlow.getChildren().clear());
    }

    private void appendToTerminal(String message, String hexColor) {
        Platform.runLater(() -> {
            Text textNode = new Text(message + "\n");
            textNode.setStyle("-fx-fill: " + hexColor + "; -fx-font-family: 'Consolas'; -fx-font-size: 14px;");
            terminalOutputFlow.getChildren().add(textNode);
        });
    }

    public void setWorkspaceId(int id) {
        this.workspaceId = id;
        System.out.println("Workspace ID received: " + this.workspaceId);

        fetchProblemById(this.workspaceId);

        if (this.problem != null) {
            problemName.setText(this.problem.getTitle());

            formTitleLabel.setText("Problem Details");
            titleLabel.setText(problem.getTitle() != null ? problem.getTitle() : "N/A");
            categoryLabel.setText(problem.getCategoryName() != null ? problem.getCategoryName() : "N/A");
            difficultyLabel.setText(problem.getDifficulty() != null ? problem.getDifficulty() : "N/A");

            if (problem.getDescription() != null) {
                String descriptionHtml = problem.getDescription() != null ? problem.getDescription() : "";

                // CSS mein !important flag lagaya hai taake rich text background override ho jaye
                String styledHtml = """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <style>
                                * {
                                    margin: 0;
                                    padding: 0;
                                    box-sizing: border-box;
                                }
                                body {
                                    font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                                    color: #C7CDD4; /* Semicolon add kar diya */
                                    background-color: transparent !important;
                                    font-size: 14px;
                                    line-height: 1.5;
                                    overflow: hidden;
                                }
                                div, p, span, h1, h2, h3, h4, h5, h6 {
                                    background-color: transparent !important;
                                }
                                p { margin-bottom: 8px; }
                                h1, h2, h3, h4, h5, h6 {
                                    color: #EEEEEE; /* HTML color code laga diya aur semicolon add kar diya */
                                    margin-top: 12px;
                                    margin-bottom: 8px;
                                    font-size: 15px;
                                }
                                ul, ol {
                                    padding-left: 20px;
                                    margin-bottom: 10px;
                                }
                                li { margin-bottom: 4px; }
                                #content-wrapper {
                                    padding: 2px;
                                }
                            </style>
                        </head>
                        <body>
                            <!-- Ye wrapper exact height calculate karne mein madad karega -->
                            <div id="content-wrapper">
                                %s
                            </div>
                        </body>
                        </html>
                        """.formatted(descriptionHtml);

                descriptionWebView.setPageFill(javafx.scene.paint.Color.TRANSPARENT);
                descriptionWebView.getEngine().loadContent(styledHtml);

                descriptionWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        adjustWebViewHeight();
                    }
                });
            }

            sampleInputLabel.setText(problem.getSampleInput() != null && !problem.getSampleInput().isBlank() ? problem.getSampleInput() : "N/A");
            sampleOutputLabel.setText(problem.getSampleOutput() != null && !problem.getSampleOutput().isBlank() ? problem.getSampleOutput() : "N/A");

        } else {
            problemName.setText("Problem Not Found");
        }
    }

    private void fetchProblemById(int id) {
        this.problem = this.problemService.getProblemById(id);
    }

    @FXML
    public void goBack(ActionEvent event) {
        SceneManager.goBack(event);
    }

    private void adjustWebViewHeight() {
        Object result = descriptionWebView.getEngine().executeScript(
                "Math.max(document.body.scrollHeight, document.body.offsetHeight, document.documentElement.clientHeight, document.documentElement.scrollHeight);"
        );

        if (result instanceof Number) {
            double height = ((Number) result).doubleValue();
            descriptionWebView.setPrefHeight(height + 15);
            descriptionWebView.setMinHeight(height + 15);
            descriptionWebView.setMaxHeight(height + 15);
        }
    }
}