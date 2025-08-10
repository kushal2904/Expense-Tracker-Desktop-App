package com.expensetracker.service;

import com.expensetracker.dao.BudgetDAO;
import com.expensetracker.dao.CategoryDAO;
import com.expensetracker.dao.ExpenseDAO;
import com.expensetracker.model.Budget;
import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Service class for handling expense-related business logic.
 */
public class ExpenseService {
    private static final Logger LOGGER = Logger.getLogger(ExpenseService.class.getName());
    
    private final ExpenseDAO expenseDAO;
    private final CategoryDAO categoryDAO;
    private final BudgetDAO budgetDAO;
    
    public ExpenseService() {
        this.expenseDAO = new ExpenseDAO();
        this.categoryDAO = new CategoryDAO();
        this.budgetDAO = new BudgetDAO();
    }
    
    public List<Expense> getAllExpenses() {
        return expenseDAO.findAll();
    }
    
    public List<Expense> getExpensesByMonth(int month, int year) {
        return expenseDAO.findByMonth(month, year);
    }
    
    public List<Expense> getExpensesByCategory(int categoryId) {
        return expenseDAO.findByCategory(categoryId);
    }
    
    public List<Expense> getExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        return expenseDAO.findByDateRange(startDate, endDate);
    }
    
    public Optional<Expense> getExpenseById(int id) {
        return expenseDAO.findById(id);
    }
    
    public boolean saveExpense(Expense expense) {
        // Validate expense data
        if (!isValidExpense(expense)) {
            return false;
        }
        
        // Check budget constraints
        BudgetValidationResult validation = validateBudget(expense);
        if (!validation.isValid()) {
            LOGGER.warning("Budget validation failed: " + validation.getMessage());
            // You might want to show a warning dialog here instead of blocking
        }
        
        return expenseDAO.save(expense);
    }
    
    public boolean deleteExpense(int id) {
        return expenseDAO.delete(id);
    }
    
    public double getTotalExpensesByMonth(int month, int year) {
        List<Expense> expenses = getExpensesByMonth(month, year);
        return expenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }
    
    public double getTotalExpensesByCategoryAndMonth(int categoryId, int month, int year) {
        return expenseDAO.getTotalByCategoryAndMonth(categoryId, month, year);
    }
    
    public BudgetValidationResult validateBudget(Expense expense) {
        int month = expense.getDate().getMonthValue();
        int year = expense.getDate().getYear();
        
        Optional<Budget> budgetOpt = budgetDAO.findByCategoryAndMonth(expense.getCategoryId(), month, year);
        if (budgetOpt.isEmpty()) {
            return new BudgetValidationResult(true, "No budget set for this category and month");
        }
        
        Budget budget = budgetOpt.get();
        double currentTotal = getTotalExpensesByCategoryAndMonth(expense.getCategoryId(), month, year);
        double newTotal = currentTotal + expense.getAmount();
        
        if (newTotal > budget.getAmount()) {
            double overBudget = newTotal - budget.getAmount();
            String message = String.format("This expense will exceed the budget by $%.2f", overBudget);
            return new BudgetValidationResult(false, message);
        }
        
        return new BudgetValidationResult(true, "Budget validation passed");
    }
    
    private boolean isValidExpense(Expense expense) {
        if (expense.getAmount() <= 0) {
            LOGGER.warning("Invalid expense amount: " + expense.getAmount());
            return false;
        }
        
        if (expense.getDate() == null) {
            LOGGER.warning("Expense date is null");
            return false;
        }
        
        if (expense.getDate().isAfter(LocalDate.now())) {
            LOGGER.warning("Expense date is in the future: " + expense.getDate());
            return false;
        }
        
        Optional<Category> category = categoryDAO.findById(expense.getCategoryId());
        if (category.isEmpty()) {
            LOGGER.warning("Invalid category ID: " + expense.getCategoryId());
            return false;
        }
        
        return true;
    }
    
    public static class BudgetValidationResult {
        private final boolean valid;
        private final String message;
        
        public BudgetValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
