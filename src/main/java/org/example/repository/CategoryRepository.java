package org.example.repository;

import org.example.model.Category;
import org.example.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {
    public List<Category> getCategories(int offset, int limit) throws SQLException {
        List<Category> categories = new ArrayList<>();

        String query = "SELECT id, name, slug, description FROM category LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, limit);
            stmt.setInt(2, offset);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Category category = new Category(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("slug"),
                        rs.getString("description")
                );
                categories.add(category);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categories;
    }

    public boolean deleteCategoryById(int id) throws SQLException{
        String query = "DELETE FROM categories WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}