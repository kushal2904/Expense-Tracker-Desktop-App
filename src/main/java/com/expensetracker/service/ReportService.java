package com.expensetracker.service;

import com.expensetracker.dao.CategoryDAO;
import com.expensetracker.dao.ExpenseDAO;
import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

/**
 * Service class for generating reports and exporting data.
 */
public class ReportService {
    private static final Logger LOGGER = Logger.getLogger(ReportService.class.getName());
    
    private final ExpenseDAO expenseDAO;
    private final CategoryDAO categoryDAO;
    private final BudgetService budgetService;
    
    public ReportService() {
        this.expenseDAO = new ExpenseDAO();
        this.categoryDAO = new CategoryDAO();
        this.budgetService = new BudgetService();
    }
    
    public MonthlyReport generateMonthlyReport(int month, int year) {
        List<Expense> expenses = expenseDAO.findByMonth(month, year);
        List<Category> categories = categoryDAO.findAll();
        
        Map<Category, Double> categoryTotals = new HashMap<>();
        double totalAmount = 0.0;
        
        // Calculate totals by category
        for (Category category : categories) {
            double categoryTotal = expenseDAO.getTotalByCategoryAndMonth(category.getId(), month, year);
            if (categoryTotal > 0) {
                categoryTotals.put(category, categoryTotal);
                totalAmount += categoryTotal;
            }
        }
        
        // Create pie chart data
        List<PieChartData> pieChartData = new ArrayList<>();
        for (Map.Entry<Category, Double> entry : categoryTotals.entrySet()) {
            Category category = entry.getKey();
            Double amount = entry.getValue();
            double percentage = totalAmount > 0 ? (amount / totalAmount) * 100 : 0;
            
            pieChartData.add(new PieChartData(category.getName(), amount, percentage, category.getColor()));
        }
        
        // Sort by amount descending
        pieChartData.sort((a, b) -> Double.compare(b.getAmount(), a.getAmount()));
        
        return new MonthlyReport(month, year, expenses, pieChartData, totalAmount);
    }
    
    public boolean exportToCSV(String filePath, int month, int year) {
        MonthlyReport report = generateMonthlyReport(month, year);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write header
            writer.println("Monthly Expense Report - " + 
                          LocalDate.of(year, month, 1).format(DateTimeFormatter.ofPattern("MMMM yyyy")));
            writer.println();
            
            // Write summary
            writer.println("Summary:");
            writer.println("Total Expenses: $" + String.format("%.2f", report.getTotalAmount()));
            writer.println("Number of Expenses: " + report.getExpenses().size());
            writer.println();
            
            // Write category breakdown
            writer.println("Category Breakdown:");
            writer.println("Category,Amount,Percentage");
            for (PieChartData data : report.getPieChartData()) {
                writer.printf("%s,$%.2f,%.1f%%%n", 
                            data.getCategoryName(), 
                            data.getAmount(), 
                            data.getPercentage());
            }
            writer.println();
            
            // Write detailed expenses
            writer.println("Detailed Expenses:");
            writer.println("Date,Category,Amount,Notes");
            
            Map<Integer, Category> categoryMap = new HashMap<>();
            for (Category category : categoryDAO.findAll()) {
                categoryMap.put(category.getId(), category);
            }
            
            for (Expense expense : report.getExpenses()) {
                Category category = categoryMap.get(expense.getCategoryId());
                String categoryName = category != null ? category.getName() : "Unknown";
                
                writer.printf("%s,%s,$%.2f,%s%n",
                            expense.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                            categoryName,
                            expense.getAmount(),
                            expense.getNotes() != null ? expense.getNotes().replace(",", ";") : "");
            }
            
            LOGGER.info("CSV export completed successfully: " + filePath);
            return true;
            
        } catch (IOException e) {
            LOGGER.severe("Error exporting to CSV: " + e.getMessage());
            return false;
        }
    }
    
    public static class MonthlyReport {
        private final int month;
        private final int year;
        private final List<Expense> expenses;
        private final List<PieChartData> pieChartData;
        private final double totalAmount;
        
        public MonthlyReport(int month, int year, List<Expense> expenses, 
                           List<PieChartData> pieChartData, double totalAmount) {
            this.month = month;
            this.year = year;
            this.expenses = expenses;
            this.pieChartData = pieChartData;
            this.totalAmount = totalAmount;
        }
        
        public int getMonth() {
            return month;
        }
        
        public int getYear() {
            return year;
        }
        
        public List<Expense> getExpenses() {
            return expenses;
        }
        
        public List<PieChartData> getPieChartData() {
            return pieChartData;
        }
        
        public double getTotalAmount() {
            return totalAmount;
        }
        
        public String getMonthYearString() {
            return LocalDate.of(year, month, 1).format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        }
    }
    
    public static class PieChartData {
        private final String categoryName;
        private final double amount;
        private final double percentage;
        private final String color;
        
        public PieChartData(String categoryName, double amount, double percentage, String color) {
            this.categoryName = categoryName;
            this.amount = amount;
            this.percentage = percentage;
            this.color = color;
        }
        
        public String getCategoryName() {
            return categoryName;
        }
        
        public double getAmount() {
            return amount;
        }
        
        public double getPercentage() {
            return percentage;
        }
        
        public String getColor() {
            return color;
        }
    }
}
