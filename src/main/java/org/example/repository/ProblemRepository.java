package org.example.repository;

import org.example.common.response.StudentProblemData;
import org.example.common.response.StudentProblemResponse;
import org.example.model.Problem;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProblemRepository {
    public List<Problem> getProblems(int offset, int limit, String q, String difficulty, Integer categoryId) {
        List<Problem> problems = new ArrayList<>();

        StringBuilder queryBuilder = new StringBuilder("SELECT p.*, c.name AS category_name " + "FROM problems p " + "LEFT JOIN category c ON p.category_id = c.id " + "WHERE (p.title LIKE ? OR c.name LIKE ?)");

        if (difficulty != null && !difficulty.trim().isEmpty() && !difficulty.equals("All")) {
            queryBuilder.append("AND p.difficulty = ? ");
        }

        if (categoryId != null && categoryId > 0) {
            queryBuilder.append("AND p.category_id = ? ");
        }

        queryBuilder.append("ORDER BY p.created_at DESC LIMIT ? OFFSET ?");

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString())) {

            String searchQuery = (q == null) ? "%" : "%" + q + "%";

            int paramIndex = 1;

            stmt.setString(paramIndex++, searchQuery);
            stmt.setString(paramIndex++, searchQuery);

            if (difficulty != null && !difficulty.trim().isEmpty() && !difficulty.equals("All")) {
                stmt.setString(paramIndex++, difficulty);
            }

            if (categoryId != null && categoryId > 0) {
                stmt.setInt(paramIndex++, categoryId);
            }

            stmt.setInt(paramIndex++, limit);
            stmt.setInt(paramIndex++, offset);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Problem problem = new Problem(rs.getInt("id"), rs.getString("title"), rs.getString("description"), rs.getString("sample_input"), rs.getString("sample_output"), rs.getInt("category_id"), rs.getString("category_name"), rs.getString("difficulty"), rs.getTimestamp("created_at"), rs.getTimestamp("updated_at"));

                problems.add(problem);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return problems;
    }

    public int getTotalProblemCount(String q, String difficulty, Integer categoryId) {
        StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(*) " + "FROM problems p " + "LEFT JOIN category c ON p.category_id = c.id " + "WHERE (p.title LIKE ? OR c.name LIKE ?)");

        if (difficulty != null && !difficulty.trim().isEmpty() && !difficulty.equals("All")) {
            queryBuilder.append("AND p.difficulty = ? ");
        }

        if (categoryId != null && categoryId > 0) {
            queryBuilder.append("AND p.category_id = ? ");
        }

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString());) {

            String searchQuery = (q == null) ? "%" : "%" + q + "%";

            int paramIndex = 1;

            stmt.setString(paramIndex++, searchQuery);
            stmt.setString(paramIndex++, searchQuery);

            if (difficulty != null && !difficulty.trim().isEmpty() && !difficulty.equals("All")) {
                stmt.setString(paramIndex++, difficulty);
            }

            if (categoryId != null && categoryId > 0) {
                stmt.setInt(paramIndex++, categoryId);
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public Problem getProblemById(int id) {
        String query = "SELECT p.*, c.name AS category_name " +
                "FROM problems p " +
                "LEFT JOIN category c ON p.category_id = c.id " +
                "WHERE p.id = ?";

        Problem problem = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                problem = new Problem(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("sample_input"),
                        rs.getString("sample_output"),
                        rs.getInt("category_id"),
                        rs.getString("category_name"),
                        rs.getString("difficulty"),
                        rs.getTimestamp("created_at"),
                        rs.getTimestamp("updated_at")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return problem;
    }

    public int saveProblem(Problem problem) {
        if (problem.getId() > 0) {
            return updateProblem(problem);
        } else {
            return insertProblem(problem);
        }
    }

    private int insertProblem(Problem problem) {
        String query = "INSERT INTO problems (title, category_id, difficulty, description, sample_input, sample_output) " + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, problem.getTitle());
            stmt.setInt(2, problem.getCategoryId());
            stmt.setString(3, problem.getDifficulty());
            stmt.setString(4, problem.getDescription());
            stmt.setString(5, problem.getSampleInput());
            stmt.setString(6, problem.getSampleOutput());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int updateProblem(Problem problem) {
        String query = "UPDATE problems SET title = ?, category_id = ?, difficulty = ?, " + "description = ?, sample_input = ?, sample_output = ?, updated_at = CURRENT_TIMESTAMP " + "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, problem.getTitle());
            stmt.setInt(2, problem.getCategoryId());
            stmt.setString(3, problem.getDifficulty());
            stmt.setString(4, problem.getDescription());
            stmt.setString(5, problem.getSampleInput());
            stmt.setString(6, problem.getSampleOutput());
            stmt.setInt(7, problem.getId());

            stmt.executeUpdate();
            return problem.getId();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean deleteProblemById(int id) {
        String query = "DELETE FROM problems WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public StudentProblemResponse getStudentDashboardProblems(
            Integer studentId, Integer limit, Integer offset, Integer categoryId, String difficulty, String q) {

        List<StudentProblemData> dataList = new ArrayList<>();
        int totalRecords = 0;
        int totalSolved = 0;

        StringBuilder queryBuilder = new StringBuilder(
                "SELECT p.id AS problemId, p.title AS title, p.difficulty AS difficulty, " +
                        "c.name AS categoryName, " +
                        "COALESCE(s.status, 'UNSOLVED') AS status, " +
                        "COUNT(*) OVER() AS totalProblems, " +
                        "COUNT(CASE WHEN s.status = 'SOLVED' THEN 1 END) OVER() AS totalSolvedProblems " +
                        "FROM problems p " +
                        "LEFT JOIN category c ON p.category_id = c.id " +
                        "LEFT JOIN (" +
                        "    SELECT problem_id, " +
                        "           CASE WHEN SUM(CASE WHEN verdict = 'PASSED' THEN 1 ELSE 0 END) > 0 THEN 'SOLVED' " +
                        "                ELSE 'ATTEMPTED' END AS status " +
                        "    FROM submissions " +
                        "    WHERE user_id = ? " +
                        "    GROUP BY problem_id" +
                        ") s ON p.id = s.problem_id " +
                        "WHERE (p.title LIKE ? OR c.name LIKE ?) "
        );

        boolean hasDifficulty = difficulty != null && !difficulty.trim().isEmpty() && !difficulty.equalsIgnoreCase("All");
        if (hasDifficulty) {
            queryBuilder.append("AND p.difficulty = ? ");
        }

        if (categoryId != null && categoryId > 0) {
            queryBuilder.append("AND p.category_id = ? ");
        }

        queryBuilder.append("ORDER BY p.created_at DESC LIMIT ? OFFSET ?");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString())) {

            int paramIndex = 1;
            stmt.setInt(paramIndex++, studentId);

            String searchQuery = (q == null || q.trim().isEmpty()) ? "%" : "%" + q + "%";
            stmt.setString(paramIndex++, searchQuery);
            stmt.setString(paramIndex++, searchQuery);

            if (hasDifficulty) {
                stmt.setString(paramIndex++, difficulty);
            }

            if (categoryId != null && categoryId > 0) {
                stmt.setInt(paramIndex++, categoryId);
            }

            stmt.setInt(paramIndex++, limit != null ? limit : 10);
            stmt.setInt(paramIndex++, offset != null ? offset : 0);

            try (ResultSet rs = stmt.executeQuery()) {
                boolean isFirstRow = true;
                while (rs.next()) {
                    if (isFirstRow) {
                        totalRecords = rs.getInt("totalProblems");
                        totalSolved = rs.getInt("totalSolvedProblems");
                        isFirstRow = false;
                    }

                    StudentProblemData data = new StudentProblemData();
                    data.setProblemId(rs.getInt("problemId"));
                    data.setTitle(rs.getString("title"));
                    data.setDifficulty(rs.getString("difficulty"));
                    data.setCategoryName(rs.getString("categoryName"));
                    data.setStatus(rs.getString("status"));

                    dataList.add(data);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new StudentProblemResponse(dataList, totalRecords, totalSolved);
    }
}