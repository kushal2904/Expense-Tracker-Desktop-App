package com.expensetracker.dao;

import com.expensetracker.model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CategoryDAO.
 */
public class CategoryDAOTest {
    
    private CategoryDAO categoryDAO;
    
    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        // Set up test database in temp directory with unique name
        String uniqueDbName = "test_db_" + System.currentTimeMillis() + ".db";
        System.setProperty("user.dir", tempDir.toString());
        System.setProperty("test.db.name", uniqueDbName);
        categoryDAO = new CategoryDAO();
    }
    
    @Test
    void testSaveNewCategory() {
        Category category = new Category("Test Category", "#FF0000");
        
        boolean result = categoryDAO.save(category);
        
        assertTrue(result);
        assertTrue(category.getId() > 0);
    }
    
    @Test
    void testFindById() {
        Category category = new Category("Test Category", "#FF0000");
        categoryDAO.save(category);
        
        Optional<Category> found = categoryDAO.findById(category.getId());
        
        assertTrue(found.isPresent());
        assertEquals(category.getName(), found.get().getName());
        assertEquals(category.getColor(), found.get().getColor());
    }
    
    @Test
    void testFindByName() {
        Category category = new Category("Test Category", "#FF0000");
        categoryDAO.save(category);
        
        Optional<Category> found = categoryDAO.findByName("Test Category");
        
        assertTrue(found.isPresent());
        assertEquals(category.getId(), found.get().getId());
    }
    
    @Test
    void testFindAll() {
        Category category1 = new Category("Category 1", "#FF0000");
        Category category2 = new Category("Category 2", "#00FF00");
        
        categoryDAO.save(category1);
        categoryDAO.save(category2);
        
        List<Category> categories = categoryDAO.findAll();
        
        assertTrue(categories.size() >= 2);
        assertTrue(categories.stream().anyMatch(c -> c.getName().equals("Category 1")));
        assertTrue(categories.stream().anyMatch(c -> c.getName().equals("Category 2")));
    }
    
    @Test
    void testUpdateCategory() {
        Category category = new Category("Original Name", "#FF0000");
        categoryDAO.save(category);
        
        category.setName("Updated Name");
        category.setColor("#00FF00");
        
        boolean result = categoryDAO.save(category);
        
        assertTrue(result);
        
        Optional<Category> updated = categoryDAO.findById(category.getId());
        assertTrue(updated.isPresent());
        assertEquals("Updated Name", updated.get().getName());
        assertEquals("#00FF00", updated.get().getColor());
    }
    
    @Test
    void testDeleteCategory() {
        Category category = new Category("Test Category", "#FF0000");
        categoryDAO.save(category);
        
        boolean result = categoryDAO.delete(category.getId());
        
        assertTrue(result);
        
        Optional<Category> deleted = categoryDAO.findById(category.getId());
        assertFalse(deleted.isPresent());
    }
    
    @Test
    void testExists() {
        Category category = new Category("Test Category", "#FF0000");
        categoryDAO.save(category);
        
        assertTrue(categoryDAO.exists("Test Category"));
        assertFalse(categoryDAO.exists("Non-existent Category"));
    }
}
