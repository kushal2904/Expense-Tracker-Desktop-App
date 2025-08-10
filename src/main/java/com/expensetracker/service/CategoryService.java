package com.expensetracker.service;

import com.expensetracker.dao.CategoryDAO;
import com.expensetracker.model.Category;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Service class for handling category-related business logic.
 */
public class CategoryService {
    private static final Logger LOGGER = Logger.getLogger(CategoryService.class.getName());
    
    private final CategoryDAO categoryDAO;
    
    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
    }
    
    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }
    
    public Optional<Category> getCategoryById(int id) {
        return categoryDAO.findById(id);
    }
    
    public Optional<Category> getCategoryByName(String name) {
        return categoryDAO.findByName(name);
    }
    
    public boolean saveCategory(Category category) {
        if (!isValidCategory(category)) {
            return false;
        }
        
        // Check if name already exists (for new categories)
        if (category.getId() == 0 && categoryDAO.exists(category.getName())) {
            LOGGER.warning("Category name already exists: " + category.getName());
            return false;
        }
        
        return categoryDAO.save(category);
    }
    
    public boolean deleteCategory(int id) {
        // TODO: Check if category is used by any expenses before deleting
        return categoryDAO.delete(id);
    }
    
    public boolean categoryExists(String name) {
        return categoryDAO.exists(name);
    }
    
    private boolean isValidCategory(Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            LOGGER.warning("Category name is null or empty");
            return false;
        }
        
        if (category.getColor() == null || category.getColor().trim().isEmpty()) {
            LOGGER.warning("Category color is null or empty");
            return false;
        }
        
        // Validate hex color format
        if (!isValidHexColor(category.getColor())) {
            LOGGER.warning("Invalid hex color format: " + category.getColor());
            return false;
        }
        
        return true;
    }
    
    private boolean isValidHexColor(String color) {
        // Remove # if present
        String hexColor = color.startsWith("#") ? color.substring(1) : color;
        
        // Check if it's a valid 6-digit hex color
        return hexColor.matches("^[0-9A-Fa-f]{6}$");
    }
}
