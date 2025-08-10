package com.expensetracker.service;

import com.expensetracker.dao.BudgetDAO;
import com.expensetracker.dao.CategoryDAO;
import com.expensetracker.dao.ExpenseDAO;
import com.expensetracker.model.Budget;
import com.expensetracker.model.Category;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Service class for handling budget-related business logic.
 */
public class BudgetService {
    private static final Logger LOGGER = Logger.getLogger(BudgetService.class.getName());
    
    private final BudgetDAO budgetDAO;
    private final CategoryDAO categoryDAO;
    private final ExpenseDAO expenseDAO;
    
    public BudgetService() {
        this.budgetDAO = new BudgetDAO();
        this.categoryDAO = new CategoryDAO();
        this.expenseDAO = new ExpenseDAO();
    }
    
    public List<Budget> getAllBudgets() {
        return budgetDAO.findAll();
    }
    
    public List<Budget> getBudgetsByMonth(int month, int year) {
        return budgetDAO.findByMonth(month, year);
    }
    
    public Optional<Budget> getBudgetByCategoryAndMonth(int categoryId, int month, int year) {
        return budgetDAO.findByCategoryAndMonth(categoryId, month, year);
    }
    
    public Optional<Budget> getBudgetById(int id) {
        return budgetDAO.findById(id);
    }
    
    public boolean saveBudget(Budget budget) {
        if (!isValidBudget(budget)) {
            return false;
        }
        
        // Check if category exists
        Optional<Category> category = categoryDAO.findById(budget.getCategoryId());
        if (category.isEmpty()) {
            LOGGER.warning("Category not found for budget: " + budget.getCategoryId());
            return false;
        }
        
        return budgetDAO.save(budget);
    }
    
    public boolean deleteBudget(int id) {
        return budgetDAO.delete(id);
    }
    
    public boolean budgetExists(int categoryId, int month, int year) {
        return budgetDAO.exists(categoryId, month, year);
    }
    
    public double getBudgetUtilization(int categoryId, int month, int year) {
        Optional<Budget> budgetOpt = getBudgetByCategoryAndMonth(categoryId, month, year);
        if (budgetOpt.isEmpty()) {
            return 0.0;
        }
        
        Budget budget = budgetOpt.get();
        double totalExpenses = expenseDAO.getTotalByCategoryAndMonth(categoryId, month, year);
        
        if (budget.getAmount() == 0) {
            return totalExpenses > 0 ? 100.0 : 0.0;
        }
        
        return (totalExpenses / budget.getAmount()) * 100.0;
    }
    
    public BudgetStatus getBudgetStatus(int categoryId, int month, int year) {
        Optional<Budget> budgetOpt = getBudgetByCategoryAndMonth(categoryId, month, year);
        if (budgetOpt.isEmpty()) {
            return new BudgetStatus(BudgetStatus.Status.NO_BUDGET, 0.0, 0.0, 0.0);
        }
        
        Budget budget = budgetOpt.get();
        double totalExpenses = expenseDAO.getTotalByCategoryAndMonth(categoryId, month, year);
        double remaining = budget.getAmount() - totalExpenses;
        double utilization = getBudgetUtilization(categoryId, month, year);
        
        BudgetStatus.Status status;
        if (remaining < 0) {
            status = BudgetStatus.Status.EXCEEDED;
        } else if (utilization >= 90) {
            status = BudgetStatus.Status.WARNING;
        } else {
            status = BudgetStatus.Status.OK;
        }
        
        return new BudgetStatus(status, budget.getAmount(), totalExpenses, remaining);
    }
    
    private boolean isValidBudget(Budget budget) {
        if (budget.getAmount() < 0) {
            LOGGER.warning("Budget amount cannot be negative: " + budget.getAmount());
            return false;
        }
        
        if (budget.getMonth() < 1 || budget.getMonth() > 12) {
            LOGGER.warning("Invalid month: " + budget.getMonth());
            return false;
        }
        
        if (budget.getYear() < 1900 || budget.getYear() > 2100) {
            LOGGER.warning("Invalid year: " + budget.getYear());
            return false;
        }
        
        return true;
    }
    
    public static class BudgetStatus {
        public enum Status {
            OK, WARNING, EXCEEDED, NO_BUDGET
        }
        
        private final Status status;
        private final double budgetAmount;
        private final double spentAmount;
        private final double remainingAmount;
        
        public BudgetStatus(Status status, double budgetAmount, double spentAmount, double remainingAmount) {
            this.status = status;
            this.budgetAmount = budgetAmount;
            this.spentAmount = spentAmount;
            this.remainingAmount = remainingAmount;
        }
        
        public Status getStatus() {
            return status;
        }
        
        public double getBudgetAmount() {
            return budgetAmount;
        }
        
        public double getSpentAmount() {
            return spentAmount;
        }
        
        public double getRemainingAmount() {
            return remainingAmount;
        }
        
        public boolean isExceeded() {
            return status == Status.EXCEEDED;
        }
        
        public boolean isWarning() {
            return status == Status.WARNING;
        }
        
        public boolean hasBudget() {
            return status != Status.NO_BUDGET;
        }
    }
}
