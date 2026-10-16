package org.example.repository;

import org.example.model.ProgressStats;
import org.example.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StatsRepository {
    public ProgressStats fetchUserProgress(int userId) {
        String sql = """
            SELECT 
                (SELECT COUNT(*) FROM problems) AS total_problems,
                (SELECT COUNT(DISTINCT problem_id) FROM submissions WHERE user_id = ? AND verdict = 'PASSED') AS total_solved,
                
                (SELECT COUNT(*) FROM problems WHERE difficulty = 'EASY') AS total_easy,
                (SELECT COUNT(DISTINCT problem_id) FROM submissions JOIN problems ON submissions.problem_id = problems.id 
                 WHERE submissions.user_id = ? AND submissions.verdict = 'PASSED' AND problems.difficulty = 'EASY') AS easy_solved,
                
                (SELECT COUNT(*) FROM problems WHERE difficulty = 'MEDIUM') AS total_medium,
                (SELECT COUNT(DISTINCT problem_id) FROM submissions JOIN problems ON submissions.problem_id = problems.id 
                 WHERE submissions.user_id = ? AND submissions.verdict = 'PASSED' AND problems.difficulty = 'MEDIUM') AS medium_solved,
                 
                (SELECT COUNT(*) FROM problems WHERE difficulty = 'HARD') AS total_hard,
                (SELECT COUNT(DISTINCT problem_id) FROM submissions JOIN problems ON submissions.problem_id = problems.id 
                 WHERE submissions.user_id = ? AND submissions.verdict = 'PASSED' AND problems.difficulty = 'HARD') AS hard_solved
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, userId);
            pstmt.setInt(4, userId);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new ProgressStats(
                        rs.getInt("total_problems"),
                        rs.getInt("total_solved"),
                        rs.getInt("total_easy"),
                        rs.getInt("easy_solved"),
                        rs.getInt("total_medium"),
                        rs.getInt("medium_solved"),
                        rs.getInt("total_hard"),
                        rs.getInt("hard_solved")
                );
            }
        } catch (Exception e) {
            System.err.println("Database Error (Fetching Stats): " + e.getMessage());
        }

        return new ProgressStats(0, 0, 0, 0, 0, 0, 0, 0);
    }
}