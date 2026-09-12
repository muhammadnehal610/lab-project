package org.example.service;

import org.example.model.Category;
import org.example.repository.CategoryRepository;

import java.sql.SQLException;
import java.util.List;

public class CategoryService {
    private final CategoryRepository categoryRepository = new CategoryRepository();

    public List<Category> getCategories(int page, int limit) {
        int offset = (page - 1) * limit;

        try {
            return this.categoryRepository.getCategories(offset, limit);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean deleteCategory(int id) {
        try {
            return this.categoryRepository.deleteCategoryById(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}