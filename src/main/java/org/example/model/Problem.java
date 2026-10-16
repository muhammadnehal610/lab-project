package org.example.model;

import java.sql.Timestamp;

public class Problem {

    private int id;
    private String title;
    private String description;
    private String sampleInput;
    private String sampleOutput;
    private int categoryId;
    private String categoryName;
    private String difficulty;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Problem() {
    }

    public Problem(String title, String description, String sampleInput, String sampleOutput, int categoryId, String difficulty) {
        this.title = title;
        this.description = description;
        this.sampleInput = sampleInput;
        this.sampleOutput = sampleOutput;
        this.categoryId = categoryId;
        this.difficulty = difficulty;
    }

    public Problem(int id, String title, String description, String sampleInput, String sampleOutput, int categoryId, String difficulty, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.sampleInput = sampleInput;
        this.sampleOutput = sampleOutput;
        this.categoryId = categoryId;
        this.difficulty = difficulty;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Problem(int id, String title, String description, String sampleInput, String sampleOutput, int categoryId, String categoryName , String difficulty, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.sampleInput = sampleInput;
        this.sampleOutput = sampleOutput;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.difficulty = difficulty;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSampleInput() {
        return sampleInput;
    }

    public void setSampleInput(String sampleInput) {
        this.sampleInput = sampleInput;
    }

    public String getSampleOutput() {
        return sampleOutput;
    }

    public void setSampleOutput(String sampleOutput) {
        this.sampleOutput = sampleOutput;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
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
        return "Problem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", categoryId=" + categoryId +
                ", difficulty='" + difficulty + '\'' +
                '}';
    }
}