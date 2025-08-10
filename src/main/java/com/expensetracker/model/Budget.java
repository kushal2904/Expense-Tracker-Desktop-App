package com.expensetracker.model;

import javafx.beans.property.*;
import java.util.Objects;

/**
 * Model class representing a monthly budget for a category.
 */
public class Budget {
    private final IntegerProperty id;
    private final IntegerProperty categoryId;
    private final DoubleProperty amount;
    private final IntegerProperty month;
    private final IntegerProperty year;

    public Budget() {
        this.id = new SimpleIntegerProperty();
        this.categoryId = new SimpleIntegerProperty();
        this.amount = new SimpleDoubleProperty();
        this.month = new SimpleIntegerProperty();
        this.year = new SimpleIntegerProperty();
    }

    public Budget(int id, int categoryId, double amount, int month, int year) {
        this.id = new SimpleIntegerProperty(id);
        this.categoryId = new SimpleIntegerProperty(categoryId);
        this.amount = new SimpleDoubleProperty(amount);
        this.month = new SimpleIntegerProperty(month);
        this.year = new SimpleIntegerProperty(year);
    }

    public Budget(int categoryId, double amount, int month, int year) {
        this.id = new SimpleIntegerProperty();
        this.categoryId = new SimpleIntegerProperty(categoryId);
        this.amount = new SimpleDoubleProperty(amount);
        this.month = new SimpleIntegerProperty(month);
        this.year = new SimpleIntegerProperty(year);
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

    // Month property
    public int getMonth() {
        return month.get();
    }

    public void setMonth(int month) {
        this.month.set(month);
    }

    public IntegerProperty monthProperty() {
        return month;
    }

    // Year property
    public int getYear() {
        return year.get();
    }

    public void setYear(int year) {
        this.year.set(year);
    }

    public IntegerProperty yearProperty() {
        return year;
    }

    @Override
    public String toString() {
        return String.format("Budget{id=%d, categoryId=%d, amount=%.2f, month=%d, year=%d}", 
                           getId(), getCategoryId(), getAmount(), getMonth(), getYear());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Budget budget = (Budget) obj;
        return getId() == budget.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
