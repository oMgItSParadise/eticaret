package source.eticaret.repository;

import source.eticaret.model.Category;
import source.eticaret.service.DatabaseService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryRepository {
    private final DatabaseService databaseService;

    public CategoryRepository(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public Optional<Category> findById(Long id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToCategory(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY name";
        
        try (Connection conn = databaseService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    public List<Category> findByParentId(Long parentId) {
        System.out.println("CategoryRepository.findByParentId(" + parentId + ")");
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE parent_id " + (parentId == null ? "IS NULL" : "= ?") + " ORDER BY name";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (parentId != null) {
                stmt.setLong(1, parentId);
            }
            
            System.out.println("Executing SQL: " + stmt.toString());
            ResultSet rs = stmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                Category category = mapResultSetToCategory(rs);
                categories.add(category);
                System.out.println("Found category: " + category.getName() + " (ID: " + category.getId() + ")");
                count++;
            }
            System.out.println("Total categories found: " + count);
            
        } catch (SQLException e) {
            System.err.println("Error in findByParentId: " + e.getMessage());
            e.printStackTrace();
        }
        return categories;
    }

    public Category save(Category category) {
        if (category.getId() == null) {
            return create(category);
        } else {
            return update(category);
        }
    }

    private Category create(Category category) {
        String sql = "INSERT INTO categories (name, description, parent_id, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            if (category.getParentId() != null) {
                stmt.setLong(3, category.getParentId());
            } else {
                stmt.setNull(3, Types.BIGINT);
            }
            stmt.setTimestamp(4, Timestamp.valueOf(category.getCreatedAt()));
            stmt.setTimestamp(5, Timestamp.valueOf(category.getUpdatedAt()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        category.setId(generatedKeys.getLong(1));
                        return category;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Category update(Category category) {
        String sql = "UPDATE categories SET name = ?, description = ?, parent_id = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            if (category.getParentId() != null) {
                stmt.setLong(3, category.getParentId());
            } else {
                stmt.setNull(3, Types.BIGINT);
            }
            stmt.setTimestamp(4, Timestamp.valueOf(category.getUpdatedAt()));
            stmt.setLong(5, category.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return category;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM categories WHERE id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getLong("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        long parentId = rs.getLong("parent_id");
        if (!rs.wasNull()) {
            category.setParentId(parentId);
        }
        category.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        category.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return category;
    }
}
