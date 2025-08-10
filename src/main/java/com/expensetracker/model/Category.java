package com.expensetracker.model;

import javafx.beans.property.*;
import java.util.Objects;

/**
 * Model class representing a category for expenses.
 */
public class Category {
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty color;

    public Category() {
        this.id = new SimpleIntegerProperty();
        this.name = new SimpleStringProperty();
        this.color = new SimpleStringProperty();
    }

    public Category(int id, String name, String color) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.color = new SimpleStringProperty(color);
    }

    public Category(String name, String color) {
        this.id = new SimpleIntegerProperty();
        this.name = new SimpleStringProperty(name);
        this.color = new SimpleStringProperty(color);
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

    // Name property
    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    // Color property
    public String getColor() {
        return color.get();
    }

    public void setColor(String color) {
        this.color.set(color);
    }

    public StringProperty colorProperty() {
        return color;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Category category = (Category) obj;
        return getId() == category.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
