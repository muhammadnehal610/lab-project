package org.example.components.problemview;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import org.example.model.Problem;
import org.example.model.TestCase;

import java.io.IOException;
import java.util.List;

public class ProblemViewModelComponent extends VBox {

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
    private Label testCaseCountLabel;

    @FXML
    private VBox testCasesContainer;

    private Problem currentProblem;

    public ProblemViewModelComponent() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/components/ProblemViewModel.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void setProblem(Problem problem, List<TestCase> testCases) {

        this.currentProblem = problem;

        if (problem == null) {
            return;
        }

        formTitleLabel.setText("Problem Details");

        titleLabel.setText(problem.getTitle() != null ? problem.getTitle() : "N/A");

        categoryLabel.setText(problem.getCategoryName() != null ? problem.getCategoryName() : "N/A");

        difficultyLabel.setText(problem.getDifficulty() != null ? problem.getDifficulty() : "N/A");

        if (problem.getDescription() != null) {
            String descriptionHtml = problem.getDescription() != null ? problem.getDescription() : "";

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
                                font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                                color: #C7CDD4; /* -fx-text-muted */
                                background-color: transparent;
                                font-size: 14px;
                                line-height: 1.6;
                                overflow: hidden;
                            }
                            p { margin-bottom: 10px; }
                            h1, h2, h3, h4, h5, h6 {
                                color: #EEEEEE; /* -fx-text-main */
                                margin-top: 12px;
                                margin-bottom: 8px;
                            }
                            code {
                                background-color: #393E46; /* -fx-input-color */
                                color: #38bdf8;
                                padding: 2px 6px;
                                border-radius: 4px;
                                font-family: 'Consolas', monospace;
                                font-size: 13px;
                            }
                            pre {
                                background-color: #393E46; /* -fx-input-color */
                                color: #EEEEEE;
                                padding: 12px;
                                border-radius: 6px;
                                border: 1px solid #4B5563; /* -fx-border-default */
                                overflow-x: auto;
                                margin: 10px 0;
                            }
                            pre code {
                                background-color: transparent;
                                padding: 0;
                                color: inherit;
                            }
                            a {
                                color: #0284c7; /* -fx-accent-color */
                                text-decoration: none;
                            }
                            ul, ol {
                                padding-left: 20px;
                                margin-bottom: 10px;
                            }
                            li { margin-bottom: 4px; }
                        </style>
                    </head>
                    <body>
                        %s
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

        showTestCases(testCases);
    }

    private void adjustWebViewHeight() {
        Object result = descriptionWebView.getEngine().executeScript(
                "Math.max(document.body.scrollHeight, document.body.offsetHeight, document.documentElement.clientHeight, document.documentElement.scrollHeight);"
        );

        if (result instanceof Number) {
            double height = ((Number) result).doubleValue();

            descriptionWebView.setPrefHeight(height + 20);
            descriptionWebView.setMinHeight(height + 20);
            descriptionWebView.setMaxHeight(height + 20);
        }
    }

    private void showTestCases(List<TestCase> testCases) {

        testCasesContainer.getChildren().clear();

        if (testCases == null || testCases.isEmpty()) {

            Label emptyLabel = new Label("No test cases available.");

            emptyLabel.getStyleClass().add("view-value-label");

            testCasesContainer.getChildren().add(emptyLabel);

            return;
        }

        for (int i = 0; i < testCases.size(); i++) {

            TestCase testCase = testCases.get(i);

            Label title = new Label("Test Case " + (i + 1));

            title.getStyleClass().add("test-case-heading");

            Label inputLabel = new Label("Input");

            inputLabel.getStyleClass().add("form-label");

            Label input = new Label(testCase.getInputData() != null ? testCase.getInputData() : "N/A");

            input.setWrapText(true);
            input.getStyleClass().add("view-code-label");

            Label outputLabel = new Label("Expected Output");

            outputLabel.getStyleClass().add("form-label");

            Label output = new Label(testCase.getExpectedOutput() != null ? testCase.getExpectedOutput() : "N/A");

            output.setWrapText(true);
            output.getStyleClass().add("view-code-label");

            HBox row = new HBox(16);

            VBox inputBox = new VBox(5, inputLabel, input);

            VBox outputBox = new VBox(5, outputLabel, output);

            HBox.setHgrow(inputBox, javafx.scene.layout.Priority.ALWAYS);

            HBox.setHgrow(outputBox, javafx.scene.layout.Priority.ALWAYS);

            inputBox.setMaxWidth(Double.MAX_VALUE);
            outputBox.setMaxWidth(Double.MAX_VALUE);

            row.getChildren().addAll(inputBox, outputBox);

            VBox testCaseBox = new VBox(8, title, row);

            testCaseBox.getStyleClass().add("test-case-box");

            testCasesContainer.getChildren().add(testCaseBox);
        }
    }
}