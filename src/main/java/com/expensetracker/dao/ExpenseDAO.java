package com.expensetracker.dao;

import com.expensetracker.model.Expense;
import com.expensetracker.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Expense entity.
 */
public class ExpenseDAO {
    private static final Logger LOGGER = Logger.getLogger(ExpenseDAO.class.getName());
    private final DatabaseManager dbManager;
    
    public ExpenseDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    public List<Expense> findAll() {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT id, amount, category_id, date, notes FROM expenses ORDER BY date DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Expense expense = new Expense(
                    rs.getInt("id"),
                    rs.getDouble("amount"),
                    rs.getInt("category_id"),
                    LocalDate.parse(rs.getString("date")),
                    rs.getString("notes")
                );
                expenses.add(expense);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all expenses", e);
        }
        
        return expenses;
    }
    
    public List<Expense> findByMonth(int month, int year) {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT id, amount, category_id, date, notes FROM expenses " +
                    "WHERE strftime('%m', date) = ? AND strftime('%Y', date) = ? " +
                    "ORDER BY date DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, String.format("%02d", month));
            stmt.setString(2, String.valueOf(year));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Expense expense = new Expense(
                        rs.getInt("id"),
                        rs.getDouble("amount"),
                        rs.getInt("category_id"),
                        LocalDate.parse(rs.getString("date")),
                        rs.getString("notes")
                    );
                    expenses.add(expense);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding expenses by month: " + month + "/" + year, e);
        }
        
        return expenses;
    }
    
    public List<Expense> findByCategory(int categoryId) {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT id, amount, category_id, date, notes FROM expenses " +
                    "WHERE category_id = ? ORDER BY date DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Expense expense = new Expense(
                        rs.getInt("id"),
                        rs.getDouble("amount"),
                        rs.getInt("category_id"),
                        LocalDate.parse(rs.getString("date")),
                        rs.getString("notes")
                    );
                    expenses.add(expense);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding expenses by category: " + categoryId, e);
        }
        
        return expenses;
    }
    
    public List<Expense> findByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT id, amount, category_id, date, notes FROM expenses " +
                    "WHERE date BETWEEN ? AND ? ORDER BY date DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, startDate.toString());
            stmt.setString(2, endDate.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Expense expense = new Expense(
                        rs.getInt("id"),
                        rs.getDouble("amount"),
                        rs.getInt("category_id"),
                        LocalDate.parse(rs.getString("date")),
                        rs.getString("notes")
                    );
                    expenses.add(expense);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding expenses by date range", e);
        }
        
        return expenses;
    }
    
    public Optional<Expense> findById(int id) {
        String sql = "SELECT id, amount, category_id, date, notes FROM expenses WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Expense expense = new Expense(
                        rs.getInt("id"),
                        rs.getDouble("amount"),
                        rs.getInt("category_id"),
                        LocalDate.parse(rs.getString("date")),
                        rs.getString("notes")
                    );
                    return Optional.of(expense);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding expense by id: " + id, e);
        }
        
        return Optional.empty();
    }
    
    public boolean save(Expense expense) {
        if (expense.getId() == 0) {
            return insert(expense);
        } else {
            return update(expense);
        }
    }
    
    private boolean insert(Expense expense) {
        String sql = "INSERT INTO expenses (amount, category_id, date, notes) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setDouble(1, expense.getAmount());
            stmt.setInt(2, expense.getCategoryId());
            stmt.setString(3, expense.getDate().toString());
            stmt.setString(4, expense.getNotes());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        expense.setId(rs.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting expense", e);
        }
        
        return false;
    }
    
    private boolean update(Expense expense) {
        String sql = "UPDATE expenses SET amount = ?, category_id = ?, date = ?, notes = ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, expense.getAmount());
            stmt.setInt(2, expense.getCategoryId());
            stmt.setString(3, expense.getDate().toString());
            stmt.setString(4, expense.getNotes());
            stmt.setInt(5, expense.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating expense", e);
        }
        
        return false;
    }
    
    public boolean delete(int id) {
        String sql = "DELETE FROM expenses WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting expense with id: " + id, e);
        }
        
        return false;
    }
    
    public double getTotalByCategoryAndMonth(int categoryId, int month, int year) {
        String sql = "SELECT SUM(amount) FROM expenses " +
                    "WHERE category_id = ? AND strftime('%m', date) = ? AND strftime('%Y', date) = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            stmt.setString(2, String.format("%02d", month));
            stmt.setString(3, String.valueOf(year));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting total by category and month", e);
        }
        
        return 0.0;
    }
}
