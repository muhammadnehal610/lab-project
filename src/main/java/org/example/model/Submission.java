package org.example.model;

public class Submission {
    private int id;
    private int userId;
    private int problemId;
    private String language;
    private String codeContent;
    private String verdict;
    private String submittedAt;

    public Submission() {
    }

    public Submission(int userId, int problemId, String language, String codeContent, String verdict) {
        this.userId = userId;
        this.problemId = problemId;
        this.language = language;
        this.codeContent = codeContent;
        this.verdict = verdict;
    }

    public Submission(int id, int userId, int problemId, String language, String codeContent, String verdict, String submittedAt) {
        this.id = id;
        this.userId = userId;
        this.problemId = problemId;
        this.language = language;
        this.codeContent = codeContent;
        this.verdict = verdict;
        this.submittedAt = submittedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getProblemId() {
        return problemId;
    }

    public void setProblemId(int problemId) {
        this.problemId = problemId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCodeContent() {
        return codeContent;
    }

    public void setCodeContent(String codeContent) {
        this.codeContent = codeContent;
    }

    public String getVerdict() {
        return verdict;
    }

    public void setVerdict(String verdict) {
        this.verdict = verdict;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(String submittedAt) {
        this.submittedAt = submittedAt;
    }
}