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
    public Category addCategory(Category category) {
        String query = "INSERT INTO category (name , slug , description) VALUES (? , ? , ?)";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, category.getName());
            stmt.setString(2, category.getSlug());
            stmt.setString(3, category.getDescription());
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                // Database se auto-generated ID get karna
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        category.setId(generatedKeys.getInt(1));
                    }
                }
                return category;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Category> getCategories(String q) {
        List<Category> categories = new ArrayList<>();

        String query = "SELECT id, name, slug, description FROM category " +
                "       WHERE name LIKE  ? " +
                "       ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

            String searchQuery = (q == null) ? "%" : "%" + q + "%";

            stmt.setString(1, searchQuery);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Category category = new Category(rs.getInt("id"), rs.getString("name"), rs.getString("slug"), rs.getString("description"));
                categories.add(category);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categories;
    }

    public List<Category> getCategoriesNames() {
        List<Category> categories = new ArrayList<>();

        String query = "SELECT id, name FROM category " +
                "       ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Category category = new Category(
                        rs.getInt("id"),
                        rs.getString("name")
                );

                categories.add(category);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categories;
    }

    public int getTotalCategoryCount(String q) {
        String query = "SELECT COUNT(*) from category WHERE name LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
        ) {
            String searchQuery = (q == null) ? "%" : "%" + q + "%";

            stmt.setString(1 , searchQuery);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public boolean deleteCategoryById(int id) {
        String query = "DELETE FROM category WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public Category updateCategory(Category category) {
        String query = "UPDATE category SET name = ?,  slug = ? , description = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, category.getName());
            stmt.setString(2, category.getSlug());
            stmt.setString(3, category.getDescription());
            stmt.setInt(4, category.getId());

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                return category;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}