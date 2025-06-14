package source.eticaret.service;

import source.eticaret.model.Category;
import source.eticaret.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(DatabaseService databaseService) {
        this.categoryRepository = new CategoryRepository(databaseService);
    }

    /**
     * Create a new category
     */
    public Category createCategory(String name, String description, Long parentId) {
        Category category = new Category(name, description);
        category.setParentId(parentId);
        return categoryRepository.save(category);
    }

    /**
     * Update an existing category
     */
    public Category updateCategory(Long id, String name, String description, Long parentId) {
        return categoryRepository.findById(id).map(category -> {
            category.setName(name);
            category.setDescription(description);
            category.setParentId(parentId);
            category.setUpdatedAt(java.time.LocalDateTime.now());
            return categoryRepository.save(category);
        }).orElse(null);
    }

    /**
     * Get category by ID
     */
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    /**
     * Get all root categories (categories with no parent)
     */
    public List<Category> getRootCategories() {
        return categoryRepository.findByParentId(null);
    }

    /**
     * Get all subcategories of a parent category
     */
    public List<Category> getSubcategories(Long parentId) {
        return categoryRepository.findByParentId(parentId);
    }

    /**
     * Get all categories
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Delete a category
     * Note: This will fail if there are products or subcategories associated with this category
     */
    public boolean deleteCategory(Long id) {

        List<Category> subcategories = categoryRepository.findByParentId(id);
        if (!subcategories.isEmpty()) {
            throw new IllegalStateException("Cannot delete category with subcategories");
        }
        

        
        return categoryRepository.delete(id);
    }

    /**
     * Move a category to a new parent
     */
    public boolean moveCategory(Long categoryId, Long newParentId) {

        if (categoryId.equals(newParentId) || isDescendant(categoryId, newParentId)) {
            return false;
        }
        
        return categoryRepository.findById(categoryId).map(category -> {
            category.setParentId(newParentId);
            category.setUpdatedAt(java.time.LocalDateTime.now());
            return categoryRepository.save(category) != null;
        }).orElse(false);
    }

    /**
     * Check if a category is a descendant of another category
     */
    private boolean isDescendant(Long parentId, Long childId) {
        if (parentId == null || childId == null) {
            return false;
        }
        
        List<Category> children = categoryRepository.findByParentId(parentId);
        for (Category child : children) {
            if (child.getId().equals(childId) || isDescendant(child.getId(), childId)) {
                return true;
            }
        }
        return false;
    }
}
