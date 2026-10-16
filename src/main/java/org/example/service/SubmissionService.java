package org.example.service;

import org.example.model.Submission;
import org.example.repository.SubmissionRepository;

public class SubmissionService {

    private final SubmissionRepository repository;

    public SubmissionService() {
        this.repository = new SubmissionRepository();
    }

    public void submitCode(int userId, int problemId, String language, String codeContent, String verdict) {

        System.out.println(language);

        Submission newSubmission = new Submission(userId, problemId, language.toUpperCase(), codeContent, verdict);

        boolean isSaved = repository.save(newSubmission);

        if (isSaved) {
            System.out.println("✅ Submission saved successfully! Verdict: " + verdict);
        } else {
            System.out.println("❌ Failed to save submission in database.");
        }
    }
}