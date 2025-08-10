package com.expensetracker.dao;

import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ExpenseDAO.
 */
public class ExpenseDAOTest {
    
    private ExpenseDAO expenseDAO;
    private CategoryDAO categoryDAO;
    private Category testCategory;
    
    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        // Set up test database in temp directory with unique name
        String uniqueDbName = "test_db_" + System.currentTimeMillis() + ".db";
        System.setProperty("user.dir", tempDir.toString());
        System.setProperty("test.db.name", uniqueDbName);
        expenseDAO = new ExpenseDAO();
        categoryDAO = new CategoryDAO();
        
        // Create a test category
        testCategory = new Category("Test Category", "#FF0000");
        categoryDAO.save(testCategory);
    }
    
    @Test
    void testSaveNewExpense() {
        Expense expense = new Expense(100.0, testCategory.getId(), LocalDate.now(), "Test expense");
        
        boolean result = expenseDAO.save(expense);
        
        assertTrue(result);
        assertTrue(expense.getId() > 0);
    }
    
    @Test
    void testFindById() {
        Expense expense = new Expense(100.0, testCategory.getId(), LocalDate.now(), "Test expense");
        expenseDAO.save(expense);
        
        Optional<Expense> found = expenseDAO.findById(expense.getId());
        
        assertTrue(found.isPresent());
        assertEquals(expense.getAmount(), found.get().getAmount());
        assertEquals(expense.getCategoryId(), found.get().getCategoryId());
        assertEquals(expense.getDate(), found.get().getDate());
        assertEquals(expense.getNotes(), found.get().getNotes());
    }
    
    @Test
    void testFindByMonth() {
        LocalDate today = LocalDate.now();
        Expense expense1 = new Expense(100.0, testCategory.getId(), today, "Expense 1");
        Expense expense2 = new Expense(200.0, testCategory.getId(), today, "Expense 2");
        
        expenseDAO.save(expense1);
        expenseDAO.save(expense2);
        
        List<Expense> expenses = expenseDAO.findByMonth(today.getMonthValue(), today.getYear());
        
        assertTrue(expenses.size() >= 2);
        assertTrue(expenses.stream().anyMatch(e -> e.getNotes().equals("Expense 1")));
        assertTrue(expenses.stream().anyMatch(e -> e.getNotes().equals("Expense 2")));
    }
    
    @Test
    void testFindByCategory() {
        Expense expense1 = new Expense(100.0, testCategory.getId(), LocalDate.now(), "Expense 1");
        Expense expense2 = new Expense(200.0, testCategory.getId(), LocalDate.now(), "Expense 2");
        
        expenseDAO.save(expense1);
        expenseDAO.save(expense2);
        
        List<Expense> expenses = expenseDAO.findByCategory(testCategory.getId());
        
        assertTrue(expenses.size() >= 2);
        assertTrue(expenses.stream().allMatch(e -> e.getCategoryId() == testCategory.getId()));
    }
    
    @Test
    void testFindByDateRange() {
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(5);
        
        Expense expense1 = new Expense(100.0, testCategory.getId(), LocalDate.now(), "Expense 1");
        Expense expense2 = new Expense(200.0, testCategory.getId(), LocalDate.now().minusDays(10), "Expense 2");
        
        expenseDAO.save(expense1);
        expenseDAO.save(expense2);
        
        List<Expense> expenses = expenseDAO.findByDateRange(startDate, endDate);
        
        assertTrue(expenses.stream().anyMatch(e -> e.getNotes().equals("Expense 1")));
        assertFalse(expenses.stream().anyMatch(e -> e.getNotes().equals("Expense 2")));
    }
    
    @Test
    void testUpdateExpense() {
        Expense expense = new Expense(100.0, testCategory.getId(), LocalDate.now(), "Original notes");
        expenseDAO.save(expense);
        
        expense.setAmount(150.0);
        expense.setNotes("Updated notes");
        
        boolean result = expenseDAO.save(expense);
        
        assertTrue(result);
        
        Optional<Expense> updated = expenseDAO.findById(expense.getId());
        assertTrue(updated.isPresent());
        assertEquals(150.0, updated.get().getAmount());
        assertEquals("Updated notes", updated.get().getNotes());
    }
    
    @Test
    void testDeleteExpense() {
        Expense expense = new Expense(100.0, testCategory.getId(), LocalDate.now(), "Test expense");
        expenseDAO.save(expense);
        
        boolean result = expenseDAO.delete(expense.getId());
        
        assertTrue(result);
        
        Optional<Expense> deleted = expenseDAO.findById(expense.getId());
        assertFalse(deleted.isPresent());
    }
    
    @Test
    void testGetTotalByCategoryAndMonth() {
        LocalDate today = LocalDate.now();
        Expense expense1 = new Expense(100.0, testCategory.getId(), today, "Expense 1");
        Expense expense2 = new Expense(200.0, testCategory.getId(), today, "Expense 2");
        
        expenseDAO.save(expense1);
        expenseDAO.save(expense2);
        
        double total = expenseDAO.getTotalByCategoryAndMonth(testCategory.getId(), 
            today.getMonthValue(), today.getYear());
        
        assertEquals(300.0, total, 0.01);
    }
}
