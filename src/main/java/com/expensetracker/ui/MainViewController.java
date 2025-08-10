package com.expensetracker.ui;

import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import com.expensetracker.service.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Controller for the main application view.
 */
public class MainViewController {
    private static final Logger LOGGER = Logger.getLogger(MainViewController.class.getName());
    
    @FXML private ListView<Category> categoryListView;
    @FXML private VBox budgetSummaryContainer;
    @FXML private Label monthYearLabel;
    @FXML private VBox chartContainer;
    @FXML private PieChart pieChart;
    @FXML private BarChart<String, Number> barChart;
    @FXML private TableView<Expense> expenseTableView;
    @FXML private TableColumn<Expense, LocalDate> dateColumn;
    @FXML private TableColumn<Expense, String> categoryColumn;
    @FXML private TableColumn<Expense, Double> amountColumn;
    @FXML private TableColumn<Expense, String> notesColumn;
    @FXML private TableColumn<Expense, Void> actionsColumn;
    @FXML private TextField searchField;
    
    private Stage primaryStage;
    private final ExpenseService expenseService;
    private final CategoryService categoryService;
    private final BudgetService budgetService;
    private final ReportService reportService;
    
    private LocalDate currentMonth;
    private ObservableList<Category> categories;
    private ObservableList<Expense> expenses;
    private FilteredList<Expense> filteredExpenses;
    
    public MainViewController() {
        this.expenseService = new ExpenseService();
        this.categoryService = new CategoryService();
        this.budgetService = new BudgetService();
        this.reportService = new ReportService();
        this.currentMonth = LocalDate.now().withDayOfMonth(1);
        this.expenses = FXCollections.observableArrayList();
        this.categories = FXCollections.observableArrayList();
    }
    
    @FXML
    public void initialize() {
        setupTableColumns();
        setupCategoryList();
        loadData();
        setupSearchFilter();
        updateMonthYearLabel();
    }
    
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    private void setupTableColumns() {
        // Date column
        dateColumn.setCellValueFactory(data -> data.getValue().dateProperty());
        dateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
                }
            }
        });
        
        // Category column
        categoryColumn.setCellValueFactory(data -> {
            int categoryId = data.getValue().getCategoryId();
            Category category = categories.stream()
                    .filter(c -> c.getId() == categoryId)
                    .findFirst()
                    .orElse(null);
            return category != null ? category.nameProperty() : null;
        });
        
        // Amount column
        amountColumn.setCellValueFactory(data -> data.getValue().amountProperty().asObject());
        amountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", amount));
                }
            }
        });
        
        // Notes column
        notesColumn.setCellValueFactory(data -> data.getValue().notesProperty());
        
        // Actions column
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            
            {
                editButton.getStyleClass().addAll("action-button", "edit-button");
                deleteButton.getStyleClass().addAll("action-button", "delete-button");
                
                editButton.setOnAction(event -> {
                    Expense expense = getTableView().getItems().get(getIndex());
                    handleEditExpense(expense);
                });
                
                deleteButton.setOnAction(event -> {
                    Expense expense = getTableView().getItems().get(getIndex());
                    handleDeleteExpense(expense);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(5, editButton, deleteButton));
                }
            }
        });
    }
    
    private void setupSearchFilter() {
        filteredExpenses = new FilteredList<>(expenses, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredExpenses.setPredicate(expense -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                // Search in category name
                Category category = categories.stream()
                        .filter(c -> c.getId() == expense.getCategoryId())
                        .findFirst()
                        .orElse(null);
                if (category != null && category.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                
                // Search in notes
                if (expense.getNotes() != null && expense.getNotes().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                
                // Search in amount
                if (String.valueOf(expense.getAmount()).contains(newValue)) {
                    return true;
                }
                
                return false;
            });
        });
        
        expenseTableView.setItems(filteredExpenses);
    }
    
    private void setupCategoryList() {
        categoryListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Category category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(10);
                    hbox.getStyleClass().add("category-item");
                    
                    // Color indicator
                    javafx.scene.shape.Rectangle colorRect = new javafx.scene.shape.Rectangle(20, 20);
                    colorRect.setFill(javafx.scene.paint.Color.web(category.getColor()));
                    colorRect.getStyleClass().add("category-color");
                    
                    // Category name
                    Label nameLabel = new Label(category.getName());
                    nameLabel.getStyleClass().add("category-name");
                    
                    hbox.getChildren().addAll(colorRect, nameLabel);
                    setGraphic(hbox);
                }
            }
        });
    }
    
    private void loadData() {
        // Load categories
        categories.clear();
        categories.addAll(categoryService.getAllCategories());
        categoryListView.setItems(categories);
        
        // Create sample data if no expenses exist
        createSampleDataIfNeeded();
        
        // Load expenses for current month
        loadExpensesForCurrentMonth();
        
        // Load budget summary
        updateBudgetSummary();
    }
    
    private void createSampleDataIfNeeded() {
        int month = currentMonth.getMonthValue();
        int year = currentMonth.getYear();
        
        List<Expense> existingExpenses = expenseService.getExpensesByMonth(month, year);
        if (existingExpenses.isEmpty() && !categories.isEmpty()) {
            // Create sample expenses for demonstration
            createSampleExpenses();
        }
    }
    
    private void createSampleExpenses() {
        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();
        
        // Sample expense data
        Object[][] sampleData = {
            {150.0, "Food & Dining", today.minusDays(1)},
            {45.0, "Transportation", today.minusDays(2)},
            {120.0, "Shopping", today.minusDays(3)},
            {80.0, "Entertainment", today.minusDays(4)},
            {200.0, "Utilities", today.minusDays(5)},
            {75.0, "Healthcare", today.minusDays(6)},
            {300.0, "Education", today.minusDays(7)},
            {60.0, "Food & Dining", today.minusDays(8)},
            {35.0, "Transportation", today.minusDays(9)},
            {95.0, "Shopping", today.minusDays(10)}
        };
        
        for (Object[] data : sampleData) {
            double amount = (Double) data[0];
            String categoryName = (String) data[1];
            LocalDate date = (LocalDate) data[2];
            
            // Find category by name
            Category category = categories.stream()
                    .filter(c -> c.getName().equals(categoryName))
                    .findFirst()
                    .orElse(categories.get(0));
            
            Expense expense = new Expense(amount, category.getId(), date, "Sample expense");
            expenseService.saveExpense(expense);
        }
    }
    
    private void loadExpensesForCurrentMonth() {
        int month = currentMonth.getMonthValue();
        int year = currentMonth.getYear();
        
        List<Expense> monthExpenses = expenseService.getExpensesByMonth(month, year);
        expenses.clear();
        expenses.addAll(monthExpenses);
        
        updatePieChart();
    }
    
    private void updatePieChart() {
        chartContainer.getChildren().clear();
        
        int month = currentMonth.getMonthValue();
        int year = currentMonth.getYear();
        
        ReportService.MonthlyReport report = reportService.generateMonthlyReport(month, year);
        
        if (report.getPieChartData().isEmpty()) {
            Label noDataLabel = new Label("No expenses for this month");
            noDataLabel.getStyleClass().add("no-data-label");
            chartContainer.getChildren().add(noDataLabel);
            return;
        }
        
        // Create a tab pane for different chart types
        TabPane chartTabPane = new TabPane();
        chartTabPane.getStyleClass().add("chart-tab-pane");
        
        // Pie Chart Tab
        Tab pieChartTab = new Tab("Pie Chart", createPieChart(report));
        pieChartTab.setClosable(false);
        
        // Bar Chart Tab
        Tab barChartTab = new Tab("Bar Chart", createBarChart(report));
        barChartTab.setClosable(false);
        
        // Line Chart Tab (Spending Trends)
        Tab lineChartTab = new Tab("Trends", createLineChart());
        lineChartTab.setClosable(false);
        
        chartTabPane.getTabs().addAll(pieChartTab, barChartTab, lineChartTab);
        chartContainer.getChildren().add(chartTabPane);
    }
    
    private PieChart createPieChart(ReportService.MonthlyReport report) {
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Expenses by Category");
        pieChart.setLegendVisible(true);
        pieChart.getStyleClass().add("expense-pie-chart");
        
        for (ReportService.PieChartData data : report.getPieChartData()) {
            PieChart.Data slice = new PieChart.Data(
                data.getCategoryName() + " ($" + String.format("%.2f", data.getAmount()) + ")",
                data.getAmount()
            );
            pieChart.getData().add(slice);
        }
        
        // Apply custom colors to pie slices
        applyPieChartColors(pieChart, report);
        
        return pieChart;
    }
    
    private void applyPieChartColors(PieChart pieChart, ReportService.MonthlyReport report) {
        String[] colors = {
            "#e74c3c", "#2ecc71", "#f39c12", "#9b59b6", 
            "#1abc9c", "#e67e22", "#34495e", "#95a5a6"
        };
        
        for (int i = 0; i < pieChart.getData().size() && i < colors.length; i++) {
            PieChart.Data slice = pieChart.getData().get(i);
            slice.getNode().setStyle("-fx-pie-color: " + colors[i] + ";");
        }
    }
    
    private BarChart<String, Number> createBarChart(ReportService.MonthlyReport report) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Categories");
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount ($)");
        
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Expenses by Category");
        barChart.getStyleClass().add("expense-bar-chart");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Expenses");
        
        for (ReportService.PieChartData data : report.getPieChartData()) {
            series.getData().add(new XYChart.Data<>(data.getCategoryName(), data.getAmount()));
        }
        
        barChart.getData().add(series);
        return barChart;
    }
    
    private LineChart<String, Number> createLineChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Days");
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount ($)");
        
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Daily Spending Trends");
        lineChart.getStyleClass().add("expense-line-chart");
        
        // Create data for the last 7 days
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Expenses");
        
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            double dailyTotal = calculateDailyTotal(date);
            String dayLabel = date.format(DateTimeFormatter.ofPattern("MMM dd"));
            series.getData().add(new XYChart.Data<>(dayLabel, dailyTotal));
        }
        
        lineChart.getData().add(series);
        return lineChart;
    }
    
    private double calculateDailyTotal(LocalDate date) {
        return expenses.stream()
                .filter(expense -> expense.getDate().equals(date))
                .mapToDouble(Expense::getAmount)
                .sum();
    }
    
    private void updateBudgetSummary() {
        budgetSummaryContainer.getChildren().clear();
        
        int month = currentMonth.getMonthValue();
        int year = currentMonth.getYear();
        
        for (Category category : categories) {
            BudgetService.BudgetStatus status = budgetService.getBudgetStatus(category.getId(), month, year);
            
            if (status.hasBudget()) {
                VBox budgetItem = new VBox(5);
                budgetItem.getStyleClass().add("budget-summary-item");
                
                Label categoryLabel = new Label(category.getName());
                categoryLabel.getStyleClass().add("budget-category-name");
                
                Label amountLabel = new Label(String.format("$%.2f / $%.2f", 
                    status.getSpentAmount(), status.getBudgetAmount()));
                amountLabel.getStyleClass().add("budget-amount");
                
                Label statusLabel = new Label();
                if (status.isExceeded()) {
                    statusLabel.setText("EXCEEDED");
                    statusLabel.getStyleClass().add("budget-status-exceeded");
                } else if (status.isWarning()) {
                    statusLabel.setText("WARNING");
                    statusLabel.getStyleClass().add("budget-status-warning");
                } else {
                    statusLabel.setText("OK");
                    statusLabel.getStyleClass().add("budget-status-ok");
                }
                
                budgetItem.getChildren().addAll(categoryLabel, amountLabel, statusLabel);
                budgetSummaryContainer.getChildren().add(budgetItem);
            }
        }
    }
    
    private void updateMonthYearLabel() {
        monthYearLabel.setText(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
    }
    
    @FXML
    private void handlePreviousMonth() {
        currentMonth = currentMonth.minusMonths(1);
        updateMonthYearLabel();
        loadExpensesForCurrentMonth();
        updateBudgetSummary();
    }
    
    @FXML
    private void handleNextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        updateMonthYearLabel();
        loadExpensesForCurrentMonth();
        updateBudgetSummary();
    }
    
    @FXML
    private void handleAddExpense() {
        showExpenseDialog(null);
    }
    
    private void handleEditExpense(Expense expense) {
        showExpenseDialog(expense);
    }
    
    private void handleDeleteExpense(Expense expense) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Expense");
        alert.setHeaderText("Are you sure you want to delete this expense?");
        alert.setContentText("This action cannot be undone.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (expenseService.deleteExpense(expense.getId())) {
                loadExpensesForCurrentMonth();
                updateBudgetSummary();
            } else {
                showError("Error", "Failed to delete expense");
            }
        }
    }
    
    private void showExpenseDialog(Expense expense) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ExpenseDialog.fxml"));
            Scene scene = new Scene(loader.load());
            
            ExpenseDialogController controller = loader.getController();
            controller.setExpense(expense);
            controller.setCategories(categories);
            controller.setCurrentMonth(currentMonth);
            
            Stage dialog = new Stage();
            dialog.setTitle(expense == null ? "Add Expense" : "Edit Expense");
            dialog.setScene(scene);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(primaryStage);
            
            dialog.showAndWait();
            
            // Refresh data after dialog closes
            loadExpensesForCurrentMonth();
            updateBudgetSummary();
            
        } catch (Exception e) {
            LOGGER.severe("Error showing expense dialog: " + e.getMessage());
            showError("Error", "Failed to open expense dialog");
        }
    }
    
    @FXML
    private void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export CSV");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        String defaultFileName = "expense_report_" + 
            currentMonth.format(DateTimeFormatter.ofPattern("yyyy_MM")) + ".csv";
        fileChooser.setInitialFileName(defaultFileName);
        
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            int month = currentMonth.getMonthValue();
            int year = currentMonth.getYear();
            
            if (reportService.exportToCSV(file.getAbsolutePath(), month, year)) {
                showInfo("Success", "CSV exported successfully to: " + file.getAbsolutePath());
            } else {
                showError("Error", "Failed to export CSV");
            }
        }
    }
    
    @FXML
    private void handleShowReports() {
        // TODO: Implement detailed reports view
        showInfo("Reports", "Detailed reports feature coming soon!");
    }
    
    @FXML
    private void handleManageCategories() {
        showCategoryManager();
    }
    
    @FXML
    private void handleManageBudgets() {
        showBudgetManager();
    }
    
    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Expense Tracker");
        alert.setContentText("Version 1.0.0\n\nA JavaFX desktop application for tracking personal expenses.\n\n" +
                           "Features:\n" +
                           "• Add, edit, delete expenses\n" +
                           "• Category management\n" +
                           "• Budget tracking\n" +
                           "• Monthly reports\n" +
                           "• CSV export");
        alert.showAndWait();
    }
    
    @FXML
    private void handleExit() {
        Platform.exit();
    }
    
    private void showCategoryManager() {
        // TODO: Implement category manager dialog
        showInfo("Categories", "Category management feature coming soon!");
    }
    
    private void showBudgetManager() {
        // TODO: Implement budget manager dialog
        showInfo("Budgets", "Budget management feature coming soon!");
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
