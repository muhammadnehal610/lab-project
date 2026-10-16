package org.example.service;

import org.example.common.response.StudentProblemData;
import org.example.common.response.StudentProblemResponse;
import org.example.model.Problem;
import org.example.model.TestCase;
import org.example.repository.ProblemRepository;
import org.example.repository.TestCaseRepository;

import java.util.List;

public class ProblemService {
    private final ProblemRepository problemRepository = new ProblemRepository();
    private final TestCaseRepository testCaseRepository = new TestCaseRepository();

    public List<Problem> getProblems(int page, int limit, String q, String difficulty, Integer categoryId) {
        int offset = (page - 1) * limit;
        return this.problemRepository.getProblems(offset, limit, q, difficulty, categoryId);
    }

    public int getTotalProblemCount(String q, String difficulty, Integer categoryId) {
        return this.problemRepository.getTotalProblemCount(q, difficulty, categoryId);
    }

    public Problem getProblemById(int id) {
        return this.problemRepository.getProblemById(id);
    }

    public boolean saveProblemWithTestCases(Problem problem, List<TestCase> testCases) {
        int savedProblemId = problemRepository.saveProblem(problem);

        if (savedProblemId != -1) {
            testCaseRepository.saveTestCases(savedProblemId, testCases);
            return true;
        }

        return false;
    }

    public boolean deleteProblemById(int id) {
        return this.problemRepository.deleteProblemById(id);
    }

    public StudentProblemResponse getStudentDashboardProblems(Integer studentId, Integer limit, Integer offset, Integer categoryId, String difficulty, String q) {
        return this.problemRepository.getStudentDashboardProblems(studentId, limit, offset, categoryId, difficulty, q);
    }
}