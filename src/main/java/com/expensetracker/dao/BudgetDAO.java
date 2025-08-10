package com.expensetracker.dao;

import com.expensetracker.model.Budget;
import com.expensetracker.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Budget entity.
 */
public class BudgetDAO {
    private static final Logger LOGGER = Logger.getLogger(BudgetDAO.class.getName());
    private final DatabaseManager dbManager;
    
    public BudgetDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    public List<Budget> findAll() {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT id, category_id, amount, month, year FROM budgets ORDER BY year DESC, month DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Budget budget = new Budget(
                    rs.getInt("id"),
                    rs.getInt("category_id"),
                    rs.getDouble("amount"),
                    rs.getInt("month"),
                    rs.getInt("year")
                );
                budgets.add(budget);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all budgets", e);
        }
        
        return budgets;
    }
    
    public List<Budget> findByMonth(int month, int year) {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT id, category_id, amount, month, year FROM budgets " +
                    "WHERE month = ? AND year = ? ORDER BY category_id";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, month);
            stmt.setInt(2, year);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Budget budget = new Budget(
                        rs.getInt("id"),
                        rs.getInt("category_id"),
                        rs.getDouble("amount"),
                        rs.getInt("month"),
                        rs.getInt("year")
                    );
                    budgets.add(budget);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding budgets by month: " + month + "/" + year, e);
        }
        
        return budgets;
    }
    
    public Optional<Budget> findByCategoryAndMonth(int categoryId, int month, int year) {
        String sql = "SELECT id, category_id, amount, month, year FROM budgets " +
                    "WHERE category_id = ? AND month = ? AND year = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            stmt.setInt(2, month);
            stmt.setInt(3, year);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Budget budget = new Budget(
                        rs.getInt("id"),
                        rs.getInt("category_id"),
                        rs.getDouble("amount"),
                        rs.getInt("month"),
                        rs.getInt("year")
                    );
                    return Optional.of(budget);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding budget by category and month", e);
        }
        
        return Optional.empty();
    }
    
    public Optional<Budget> findById(int id) {
        String sql = "SELECT id, category_id, amount, month, year FROM budgets WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Budget budget = new Budget(
                        rs.getInt("id"),
                        rs.getInt("category_id"),
                        rs.getDouble("amount"),
                        rs.getInt("month"),
                        rs.getInt("year")
                    );
                    return Optional.of(budget);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding budget by id: " + id, e);
        }
        
        return Optional.empty();
    }
    
    public boolean save(Budget budget) {
        if (budget.getId() == 0) {
            return insert(budget);
        } else {
            return update(budget);
        }
    }
    
    private boolean insert(Budget budget) {
        String sql = "INSERT INTO budgets (category_id, amount, month, year) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, budget.getCategoryId());
            stmt.setDouble(2, budget.getAmount());
            stmt.setInt(3, budget.getMonth());
            stmt.setInt(4, budget.getYear());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        budget.setId(rs.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting budget", e);
        }
        
        return false;
    }
    
    private boolean update(Budget budget) {
        String sql = "UPDATE budgets SET category_id = ?, amount = ?, month = ?, year = ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, budget.getCategoryId());
            stmt.setDouble(2, budget.getAmount());
            stmt.setInt(3, budget.getMonth());
            stmt.setInt(4, budget.getYear());
            stmt.setInt(5, budget.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating budget", e);
        }
        
        return false;
    }
    
    public boolean delete(int id) {
        String sql = "DELETE FROM budgets WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting budget with id: " + id, e);
        }
        
        return false;
    }
    
    public boolean exists(int categoryId, int month, int year) {
        return findByCategoryAndMonth(categoryId, month, year).isPresent();
    }
}
