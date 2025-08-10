package com.expensetracker.util;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for managing SQLite database connections and initialization.
 */
public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static final String DB_URL = getDatabaseUrl();
    
    private static String getDatabaseUrl() {
        String testDbName = System.getProperty("test.db.name");
        if (testDbName != null) {
            return "jdbc:sqlite:" + testDbName;
        }
        return "jdbc:sqlite:expense_tracker.db";
    }
    
    private static DatabaseManager instance;
    private Connection connection;
    
    private DatabaseManager() {
        initializeDatabase();
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }
    
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing database connection", e);
            }
        }
    }
    
    private void initializeDatabase() {
        try (Connection conn = getConnection()) {
            createTables(conn);
            insertSeedData(conn);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error initializing database", e);
        }
    }
    
    private void createTables(Connection conn) throws SQLException {
        // Create categories table
        String createCategoriesTable = """
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT UNIQUE NOT NULL,
                color TEXT NOT NULL
            )
            """;
        
        // Create budgets table
        String createBudgetsTable = """
            CREATE TABLE IF NOT EXISTS budgets (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category_id INTEGER NOT NULL,
                amount REAL NOT NULL,
                month INTEGER NOT NULL,
                year INTEGER NOT NULL,
                FOREIGN KEY (category_id) REFERENCES categories (id),
                UNIQUE(category_id, month, year)
            )
            """;
        
        // Create expenses table
        String createExpensesTable = """
            CREATE TABLE IF NOT EXISTS expenses (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                amount REAL NOT NULL,
                category_id INTEGER NOT NULL,
                date TEXT NOT NULL,
                notes TEXT,
                FOREIGN KEY (category_id) REFERENCES categories (id)
            )
            """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createCategoriesTable);
            stmt.execute(createBudgetsTable);
            stmt.execute(createExpensesTable);
        }
    }
    
    private void insertSeedData(Connection conn) throws SQLException {
        // Check if categories table is empty
        String checkCategories = "SELECT COUNT(*) FROM categories";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkCategories)) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Insert seed categories
                String[] categories = {
                    "INSERT INTO categories (name, color) VALUES ('Food & Dining', '#FF6B6B')",
                    "INSERT INTO categories (name, color) VALUES ('Transportation', '#4ECDC4')",
                    "INSERT INTO categories (name, color) VALUES ('Shopping', '#45B7D1')",
                    "INSERT INTO categories (name, color) VALUES ('Entertainment', '#96CEB4')",
                    "INSERT INTO categories (name, color) VALUES ('Utilities', '#FFEAA7')",
                    "INSERT INTO categories (name, color) VALUES ('Healthcare', '#DDA0DD')",
                    "INSERT INTO categories (name, color) VALUES ('Education', '#98D8C8')",
                    "INSERT INTO categories (name, color) VALUES ('Other', '#F7DC6F')"
                };
                
                for (String sql : categories) {
                    try (Statement insertStmt = conn.createStatement()) {
                        insertStmt.execute(sql);
                    }
                }
                
                LOGGER.info("Seed data inserted successfully");
            }
        }
    }
}
