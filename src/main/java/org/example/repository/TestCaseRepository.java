package org.example.repository;

import org.example.model.TestCase;
import org.example.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TestCaseRepository {
    public void saveTestCases(int problemId, List<TestCase> testCases) {
        deleteByProblemId(problemId);

        if (testCases == null || testCases.isEmpty()) {
            return;
        }

        String query = "INSERT INTO test_cases (problem_id, input_data, expected_output) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (TestCase tc : testCases) {
                stmt.setInt(1, problemId);
                stmt.setString(2, tc.getInputData());
                stmt.setString(3, tc.getExpectedOutput());

                stmt.addBatch();
            }

            stmt.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<TestCase> getTestCasesByProblemId(int problemId) {
        List<TestCase> testCases = new ArrayList<>();

        String query = "SELECT * FROM test_cases WHERE problem_id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query);) {
            stmt.setInt(1, problemId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                TestCase testCase = new TestCase(rs.getInt("problem_id"), rs.getString("input_data"), rs.getString("expected_output"));

                testCases.add(testCase);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return testCases;
    }

    public void deleteByProblemId(int problemId) {
        String query = "DELETE FROM test_cases WHERE problem_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, problemId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}