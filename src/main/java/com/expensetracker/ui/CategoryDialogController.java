package com.expensetracker.ui;

import com.expensetracker.model.Category;
import com.expensetracker.service.CategoryService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.ColorPicker;
import javafx.stage.Stage;

import java.util.logging.Logger;

/**
 * Controller for the category dialog.
 */
public class CategoryDialogController {
    private static final Logger LOGGER = Logger.getLogger(CategoryDialogController.class.getName());
    
    @FXML private Label dialogTitle;
    @FXML private TextField nameField;
    @FXML private TextField colorField;
    @FXML private Rectangle colorPreview;
    
    private Category category;
    private final CategoryService categoryService;
    private Stage dialogStage;
    
    public CategoryDialogController() {
        this.categoryService = new CategoryService();
    }
    
    @FXML
    public void initialize() {
        setupValidation();
        setupColorPreview();
    }
    
    public void setCategory(Category category) {
        this.category = category;
        if (category != null) {
            dialogTitle.setText("Edit Category");
            populateFields();
        } else {
            dialogTitle.setText("Add Category");
            clearFields();
        }
    }
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    private void setupValidation() {
        // Color field validation - only allow hex characters
        colorField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[#]?[0-9A-Fa-f]*")) {
                colorField.setText(oldValue);
            }
        });
    }
    
    private void setupColorPreview() {
        colorField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateColorPreview(newValue);
        });
    }
    
    private void updateColorPreview(String colorText) {
        try {
            if (colorText != null && !colorText.isEmpty()) {
                String hexColor = colorText.startsWith("#") ? colorText : "#" + colorText;
                if (hexColor.matches("#[0-9A-Fa-f]{6}")) {
                    Color color = Color.web(hexColor);
                    colorPreview.setFill(color);
                }
            }
        } catch (Exception e) {
            // Invalid color format, ignore
        }
    }
    
    private void populateFields() {
        nameField.setText(category.getName());
        colorField.setText(category.getColor());
        updateColorPreview(category.getColor());
    }
    
    private void clearFields() {
        nameField.clear();
        colorField.clear();
        colorPreview.setFill(Color.TRANSPARENT);
    }
    
    @FXML
    private void handlePickColor() {
        ColorPicker colorPicker = new ColorPicker();
        if (category != null && category.getColor() != null) {
            try {
                colorPicker.setValue(Color.web(category.getColor()));
            } catch (Exception e) {
                colorPicker.setValue(Color.BLACK);
            }
        }
        
        colorPicker.setOnAction(event -> {
            Color selectedColor = colorPicker.getValue();
            String hexColor = String.format("#%02X%02X%02X",
                (int) (selectedColor.getRed() * 255),
                (int) (selectedColor.getGreen() * 255),
                (int) (selectedColor.getBlue() * 255));
            colorField.setText(hexColor);
        });
        
        // Show color picker in a new window
        Stage colorStage = new Stage();
        colorStage.setTitle("Pick Color");
        colorStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        colorStage.initOwner(dialogStage);
        
        javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(10);
        vbox.setPadding(new javafx.geometry.Insets(20));
        vbox.getChildren().addAll(new Label("Select a color:"), colorPicker);
        
        javafx.scene.Scene scene = new javafx.scene.Scene(vbox);
        colorStage.setScene(scene);
        colorStage.showAndWait();
    }
    
    @FXML
    private void handleSave() {
        if (validateInput()) {
            Category categoryToSave = category != null ? category : new Category();
            
            categoryToSave.setName(nameField.getText().trim());
            categoryToSave.setColor(normalizeColor(colorField.getText()));
            
            if (categoryService.saveCategory(categoryToSave)) {
                closeDialog();
            } else {
                showError("Error", "Failed to save category. Name might already exist.");
            }
        }
    }
    
    @FXML
    private void handleCancel() {
        closeDialog();
    }
    
    private String normalizeColor(String colorText) {
        if (colorText == null || colorText.trim().isEmpty()) {
            return "#000000";
        }
        
        String color = colorText.trim();
        if (!color.startsWith("#")) {
            color = "#" + color;
        }
        
        // Ensure it's a valid 6-digit hex color
        if (color.matches("#[0-9A-Fa-f]{6}")) {
            return color.toUpperCase();
        } else {
            return "#000000";
        }
    }
    
    private boolean validateInput() {
        // Validate name
        if (nameField.getText().trim().isEmpty()) {
            showError("Validation Error", "Please enter a category name");
            nameField.requestFocus();
            return false;
        }
        
        // Validate color
        if (colorField.getText().trim().isEmpty()) {
            showError("Validation Error", "Please enter a color");
            colorField.requestFocus();
            return false;
        }
        
        String normalizedColor = normalizeColor(colorField.getText());
        if (normalizedColor.equals("#000000") && !colorField.getText().trim().equals("#000000")) {
            showError("Validation Error", "Please enter a valid hex color (e.g., #FF0000)");
            colorField.requestFocus();
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
