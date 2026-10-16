package org.example.service;

import org.example.model.TestCase;
import org.example.repository.TestCaseRepository;

import java.util.List;

public class TestCaseService {
    private final TestCaseRepository testCaseRepository = new TestCaseRepository();

    public List<TestCase> getTestCasesByProblemId(int problemId) {
        return this.testCaseRepository.getTestCasesByProblemId(problemId);
    }
}
