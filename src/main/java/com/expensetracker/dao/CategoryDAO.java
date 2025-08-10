package com.expensetracker.dao;

import com.expensetracker.model.Category;
import com.expensetracker.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Category entity.
 */
public class CategoryDAO {
    private static final Logger LOGGER = Logger.getLogger(CategoryDAO.class.getName());
    private final DatabaseManager dbManager;
    
    public CategoryDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT id, name, color FROM categories ORDER BY name";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Category category = new Category(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("color")
                );
                categories.add(category);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all categories", e);
        }
        
        return categories;
    }
    
    public Optional<Category> findById(int id) {
        String sql = "SELECT id, name, color FROM categories WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Category category = new Category(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("color")
                    );
                    return Optional.of(category);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding category by id: " + id, e);
        }
        
        return Optional.empty();
    }
    
    public Optional<Category> findByName(String name) {
        String sql = "SELECT id, name, color FROM categories WHERE name = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Category category = new Category(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("color")
                    );
                    return Optional.of(category);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding category by name: " + name, e);
        }
        
        return Optional.empty();
    }
    
    public boolean save(Category category) {
        if (category.getId() == 0) {
            return insert(category);
        } else {
            return update(category);
        }
    }
    
    private boolean insert(Category category) {
        String sql = "INSERT INTO categories (name, color) VALUES (?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getColor());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        category.setId(rs.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting category", e);
        }
        
        return false;
    }
    
    private boolean update(Category category) {
        String sql = "UPDATE categories SET name = ?, color = ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getColor());
            stmt.setInt(3, category.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating category", e);
        }
        
        return false;
    }
    
    public boolean delete(int id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting category with id: " + id, e);
        }
        
        return false;
    }
    
    public boolean exists(String name) {
        return findByName(name).isPresent();
    }
}
