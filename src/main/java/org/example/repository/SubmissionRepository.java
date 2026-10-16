package org.example.repository;

import org.example.model.Submission;
import org.example.util.DatabaseConnection;

import java.sql.*;

public class SubmissionRepository {

    public boolean save(Submission submission) {
        String sql = "INSERT INTO submissions " +
                "(user_id, problem_id, language, code_content, verdict) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, submission.getUserId());
            pstmt.setInt(2, submission.getProblemId());
            pstmt.setString(3, submission.getLanguage());
            pstmt.setString(4, submission.getCodeContent());
            pstmt.setString(5, submission.getVerdict());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Submission save karte waqt error: " + e.getMessage());
            return false;
        }
    }
}
