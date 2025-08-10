package com.expensetracker.ui;

import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import com.expensetracker.service.ExpenseService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Controller for the expense dialog.
 */
public class ExpenseDialogController {
    private static final Logger LOGGER = Logger.getLogger(ExpenseDialogController.class.getName());
    
    @FXML private Label dialogTitle;
    @FXML private TextField amountField;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextArea notesTextArea;
    @FXML private VBox budgetWarningContainer;
    @FXML private Label budgetWarningLabel;
    
    private Expense expense;
    private ObservableList<Category> categories;
    private LocalDate currentMonth;
    private final ExpenseService expenseService;
    private Stage dialogStage;
    
    public ExpenseDialogController() {
        this.expenseService = new ExpenseService();
    }
    
    @FXML
    public void initialize() {
        setupValidation();
        setupBudgetWarning();
    }
    
    public void setExpense(Expense expense) {
        this.expense = expense;
        if (expense != null) {
            dialogTitle.setText("Edit Expense");
            populateFields();
        } else {
            dialogTitle.setText("Add Expense");
            clearFields();
        }
    }
    
    public void setCategories(ObservableList<Category> categories) {
        this.categories = categories;
        categoryComboBox.setItems(categories);
    }
    
    public void setCurrentMonth(LocalDate currentMonth) {
        this.currentMonth = currentMonth;
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
        
        // Category selection validation
        categoryComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                validateBudget();
            }
        });
        
        // Amount change validation
        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && categoryComboBox.getValue() != null) {
                validateBudget();
            }
        });
    }
    
    private void setupBudgetWarning() {
        budgetWarningContainer.setVisible(false);
        budgetWarningContainer.setManaged(false);
    }
    
    private void populateFields() {
        amountField.setText(String.format("%.2f", expense.getAmount()));
        
        Category selectedCategory = categories.stream()
                .filter(c -> c.getId() == expense.getCategoryId())
                .findFirst()
                .orElse(null);
        categoryComboBox.setValue(selectedCategory);
        
        datePicker.setValue(expense.getDate());
        notesTextArea.setText(expense.getNotes());
    }
    
    private void clearFields() {
        amountField.clear();
        categoryComboBox.setValue(null);
        datePicker.setValue(LocalDate.now());
        notesTextArea.clear();
        budgetWarningContainer.setVisible(false);
        budgetWarningContainer.setManaged(false);
    }
    
    private void validateBudget() {
        try {
            double amount = Double.parseDouble(amountField.getText());
            Category selectedCategory = categoryComboBox.getValue();
            
            if (selectedCategory != null) {
                Expense tempExpense = new Expense(amount, selectedCategory.getId(), 
                    datePicker.getValue(), notesTextArea.getText());
                
                ExpenseService.BudgetValidationResult result = expenseService.validateBudget(tempExpense);
                
                if (!result.isValid()) {
                    budgetWarningLabel.setText(result.getMessage());
                    budgetWarningContainer.setVisible(true);
                    budgetWarningContainer.setManaged(true);
                } else {
                    budgetWarningContainer.setVisible(false);
                    budgetWarningContainer.setManaged(false);
                }
            }
        } catch (NumberFormatException e) {
            // Amount field is empty or invalid, hide warning
            budgetWarningContainer.setVisible(false);
            budgetWarningContainer.setManaged(false);
        }
    }
    
    @FXML
    private void handleSave() {
        if (validateInput()) {
            Expense expenseToSave = expense != null ? expense : new Expense();
            
            try {
                expenseToSave.setAmount(Double.parseDouble(amountField.getText()));
                expenseToSave.setCategoryId(categoryComboBox.getValue().getId());
                expenseToSave.setDate(datePicker.getValue());
                expenseToSave.setNotes(notesTextArea.getText());
                
                if (expenseService.saveExpense(expenseToSave)) {
                    closeDialog();
                } else {
                    showError("Error", "Failed to save expense");
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
        // Validate amount
        if (amountField.getText().isEmpty()) {
            showError("Validation Error", "Please enter an amount");
            amountField.requestFocus();
            return false;
        }
        
        try {
            double amount = Double.parseDouble(amountField.getText());
            if (amount <= 0) {
                showError("Validation Error", "Amount must be greater than zero");
                amountField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Validation Error", "Please enter a valid amount");
            amountField.requestFocus();
            return false;
        }
        
        // Validate category
        if (categoryComboBox.getValue() == null) {
            showError("Validation Error", "Please select a category");
            categoryComboBox.requestFocus();
            return false;
        }
        
        // Validate date
        if (datePicker.getValue() == null) {
            showError("Validation Error", "Please select a date");
            datePicker.requestFocus();
            return false;
        }
        
        if (datePicker.getValue().isAfter(LocalDate.now())) {
            showError("Validation Error", "Date cannot be in the future");
            datePicker.requestFocus();
            return false;
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
