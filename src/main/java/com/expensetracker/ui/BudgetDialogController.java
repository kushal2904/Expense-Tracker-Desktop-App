package com.expensetracker.ui;

import com.expensetracker.model.Budget;
import com.expensetracker.model.Category;
import com.expensetracker.service.BudgetService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.Month;
import java.util.logging.Logger;

/**
 * Controller for the budget dialog.
 */
public class BudgetDialogController {
    private static final Logger LOGGER = Logger.getLogger(BudgetDialogController.class.getName());
    
    @FXML private Label dialogTitle;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private TextField amountField;
    @FXML private ComboBox<Month> monthComboBox;
    @FXML private Spinner<Integer> yearSpinner;
    
    private Budget budget;
    private ObservableList<Category> categories;
    private final BudgetService budgetService;
    private Stage dialogStage;
    
    public BudgetDialogController() {
        this.budgetService = new BudgetService();
    }
    
    @FXML
    public void initialize() {
        setupValidation();
        setupMonthComboBox();
        setupYearSpinner();
    }
    
    public void setBudget(Budget budget) {
        this.budget = budget;
        if (budget != null) {
            dialogTitle.setText("Edit Budget");
            populateFields();
        } else {
            dialogTitle.setText("Add Budget");
            clearFields();
        }
    }
    
    public void setCategories(ObservableList<Category> categories) {
        this.categories = categories;
        categoryComboBox.setItems(categories);
    }
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    private void setupValidation() {
        // Amount validation - only allow numbers and decimal point
        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                amountField.setText(oldValue);
            }
        });
    }
    
    private void setupMonthComboBox() {
        monthComboBox.setItems(FXCollections.observableArrayList(Month.values()));
        monthComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Month month, boolean empty) {
                super.updateItem(month, empty);
                if (empty || month == null) {
                    setText(null);
                } else {
                    setText(month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault()));
                }
            }
        });
        
        monthComboBox.setButtonCell(monthComboBox.getCellFactory().call(null));
    }
    
    private void setupYearSpinner() {
        SpinnerValueFactory<Integer> yearFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
            2020, 2030, java.time.LocalDate.now().getYear());
        yearSpinner.setValueFactory(yearFactory);
        yearSpinner.setEditable(true);
        
        // Allow manual input
        yearSpinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                yearSpinner.getEditor().setText(oldValue);
            }
        });
    }
    
    private void populateFields() {
        Category selectedCategory = categories.stream()
                .filter(c -> c.getId() == budget.getCategoryId())
                .findFirst()
                .orElse(null);
        categoryComboBox.setValue(selectedCategory);
        
        amountField.setText(String.format("%.2f", budget.getAmount()));
        monthComboBox.setValue(Month.of(budget.getMonth()));
        yearSpinner.getValueFactory().setValue(budget.getYear());
    }
    
    private void clearFields() {
        categoryComboBox.setValue(null);
        amountField.clear();
        monthComboBox.setValue(Month.of(java.time.LocalDate.now().getMonthValue()));
        yearSpinner.getValueFactory().setValue(java.time.LocalDate.now().getYear());
    }
    
    @FXML
    private void handleSave() {
        if (validateInput()) {
            Budget budgetToSave = budget != null ? budget : new Budget();
            
            try {
                budgetToSave.setCategoryId(categoryComboBox.getValue().getId());
                budgetToSave.setAmount(Double.parseDouble(amountField.getText()));
                budgetToSave.setMonth(monthComboBox.getValue().getValue());
                budgetToSave.setYear(yearSpinner.getValue());
                
                if (budgetService.saveBudget(budgetToSave)) {
                    closeDialog();
                } else {
                    showError("Error", "Failed to save budget");
                }
            } catch (NumberFormatException e) {
                showError("Error", "Invalid amount format");
            }
        }
    }
    
    @FXML
    private void handleCancel() {
        closeDialog();
    }
    
    private boolean validateInput() {
        // Validate category
        if (categoryComboBox.getValue() == null) {
            showError("Validation Error", "Please select a category");
            categoryComboBox.requestFocus();
            return false;
        }
        
        // Validate amount
        if (amountField.getText().isEmpty()) {
            showError("Validation Error", "Please enter an amount");
            amountField.requestFocus();
            return false;
        }
        
        try {
            double amount = Double.parseDouble(amountField.getText());
            if (amount < 0) {
                showError("Validation Error", "Amount cannot be negative");
                amountField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Validation Error", "Please enter a valid amount");
            amountField.requestFocus();
            return false;
        }
        
        // Validate month
        if (monthComboBox.getValue() == null) {
            showError("Validation Error", "Please select a month");
            monthComboBox.requestFocus();
            return false;
        }
        
        // Validate year
        if (yearSpinner.getValue() == null) {
            showError("Validation Error", "Please enter a valid year");
            yearSpinner.requestFocus();
            return false;
        }
        
        // Check if budget already exists for this category and month/year
        if (budget == null) { // Only check for new budgets
            int categoryId = categoryComboBox.getValue().getId();
            int month = monthComboBox.getValue().getValue();
            int year = yearSpinner.getValue();
            
            if (budgetService.budgetExists(categoryId, month, year)) {
                showError("Validation Error", "A budget already exists for this category and month/year");
                return false;
            }
        }
        
        return true;
    }
    
    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
