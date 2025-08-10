package com.expensetracker.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main JavaFX application class for the Expense Tracker.
 */
public class ExpenseTrackerApp extends Application {
    private static final Logger LOGGER = Logger.getLogger(ExpenseTrackerApp.class.getName());
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the main FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Parent root = loader.load();
            
            // Get the controller
            MainViewController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            
            // Set up the scene
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            // Configure the stage
            primaryStage.setTitle("Expense Tracker");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(600);
            
            // Show the stage
            primaryStage.show();
            
            LOGGER.info("Expense Tracker application started successfully");
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error starting application", e);
            System.exit(1);
        }
    }
    
    @Override
    public void stop() {
        // Clean up resources when application is closing
        LOGGER.info("Expense Tracker application is shutting down");
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
