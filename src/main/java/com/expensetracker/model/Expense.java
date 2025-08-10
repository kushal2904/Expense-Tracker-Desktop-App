package com.expensetracker.model;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Model class representing an expense.
 */
public class Expense {
    private final IntegerProperty id;
    private final DoubleProperty amount;
    private final IntegerProperty categoryId;
    private final ObjectProperty<LocalDate> date;
    private final StringProperty notes;

    public Expense() {
        this.id = new SimpleIntegerProperty();
        this.amount = new SimpleDoubleProperty();
        this.categoryId = new SimpleIntegerProperty();
        this.date = new SimpleObjectProperty<>();
        this.notes = new SimpleStringProperty();
    }

    public Expense(int id, double amount, int categoryId, LocalDate date, String notes) {
        this.id = new SimpleIntegerProperty(id);
        this.amount = new SimpleDoubleProperty(amount);
        this.categoryId = new SimpleIntegerProperty(categoryId);
        this.date = new SimpleObjectProperty<>(date);
        this.notes = new SimpleStringProperty(notes);
    }

    public Expense(double amount, int categoryId, LocalDate date, String notes) {
        this.id = new SimpleIntegerProperty();
        this.amount = new SimpleDoubleProperty(amount);
        this.categoryId = new SimpleIntegerProperty(categoryId);
        this.date = new SimpleObjectProperty<>(date);
        this.notes = new SimpleStringProperty(notes);
    }

    // ID property
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    // Amount property
    public double getAmount() {
        return amount.get();
    }

    public void setAmount(double amount) {
        this.amount.set(amount);
    }

    public DoubleProperty amountProperty() {
        return amount;
    }

    // Category ID property
    public int getCategoryId() {
        return categoryId.get();
    }

    public void setCategoryId(int categoryId) {
        this.categoryId.set(categoryId);
    }

    public IntegerProperty categoryIdProperty() {
        return categoryId;
    }

    // Date property
    public LocalDate getDate() {
        return date.get();
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    // Notes property
    public String getNotes() {
        return notes.get();
    }

    public void setNotes(String notes) {
        this.notes.set(notes);
    }

    public StringProperty notesProperty() {
        return notes;
    }

    @Override
    public String toString() {
        return String.format("Expense{id=%d, amount=%.2f, categoryId=%d, date=%s, notes='%s'}", 
                           getId(), getAmount(), getCategoryId(), getDate(), getNotes());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Expense expense = (Expense) obj;
        return getId() == expense.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
