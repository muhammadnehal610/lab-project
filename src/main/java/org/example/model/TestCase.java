package org.example.model;

import java.sql.Timestamp;

public class TestCase {

    private int id;
    private String inputData;
    private String expectedOutput;
    private int problemId;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public TestCase() {
    }

    public TestCase( int problemId, String inputData, String expectedOutput) {
        this.inputData = inputData;
        this.expectedOutput = expectedOutput;
        this.problemId = problemId;
    }

    public TestCase(int id, String inputData, String expectedOutput, int problemId, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.inputData = inputData;
        this.expectedOutput = expectedOutput;
        this.problemId = problemId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // --- Getters and Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInputData() {
        return inputData;
    }

    public void setInputData(String inputData) {
        this.inputData = inputData;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }

    public int getProblemId() {
        return problemId;
    }

    public void setProblemId(int problemId) {
        this.problemId = problemId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "TestCase{" +
                "id=" + id +
                ", problemId=" + problemId +
                ", inputData='" + (inputData != null ? inputData.replace("\n", "\\n") : "null") + '\'' +
                ", expectedOutput='" + (expectedOutput != null ? expectedOutput.replace("\n", "\\n") : "null") + '\'' +
                '}';
    }
}