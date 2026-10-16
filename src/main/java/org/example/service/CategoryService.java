package org.example.service;

import org.example.model.Category;
import org.example.repository.CategoryRepository;

import java.sql.SQLException;
import java.util.List;

public class CategoryService {
    private final CategoryRepository categoryRepository = new CategoryRepository();

    public Category addCategory(Category category) {
        return this.categoryRepository.addCategory(category);
    }

    public List<Category> getCategories( String q) {
        return this.categoryRepository.getCategories( q);
    }

    public List<Category> getCategoriesNames() {
        return this.categoryRepository.getCategoriesNames();
    }

    public int getTotalCategoryCount(String q) {
        return this.categoryRepository.getTotalCategoryCount(q);
    }

    public boolean deleteCategory(int id) {
        return this.categoryRepository.deleteCategoryById(id);
    }

    public Category updateCategory(Category category) {
        return this.categoryRepository.updateCategory(category);
    }
}