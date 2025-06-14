package source.eticaret.repository;

import source.eticaret.model.CartItem;
import source.eticaret.model.Product;
import source.eticaret.service.DatabaseService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CartRepository {
    private final DatabaseService databaseService;
    private final ProductRepository productRepository;

    public CartRepository(DatabaseService databaseService) {
        this.databaseService = databaseService;
        this.productRepository = new ProductRepository(databaseService);
    }

    public List<CartItem> findByUserId(Long userId) {
        List<CartItem> cartItems = new ArrayList<>();
        if (userId == null) return cartItems;
        
        String sql = "SELECT ci.*, p.name as product_name, p.price, p.stock_quantity, p.description, p.image_url, p.category_id, p.created_at as product_created_at, p.updated_at as product_updated_at " +
                   "FROM cart_items ci " +
                   "LEFT JOIN products p ON ci.product_id = p.id " +
                   "WHERE ci.user_id = ?";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CartItem item = mapResultSetToCartItemWithProduct(rs);
                    if (item != null) {
                        cartItems.add(item);
                    }
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return cartItems;
    }

    public int countByUserId(Long userId) {
        if (userId == null) return 0;
        
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM cart_items WHERE user_id = ?";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }

    public Optional<CartItem> findById(Long id) {
        if (id == null) return Optional.empty();
        
        String sql = "SELECT ci.*, p.name as product_name, p.price, p.stock_quantity, p.description, p.image_url, p.category_id, p.created_at as product_created_at, p.updated_at as product_updated_at " +
                   "FROM cart_items ci " +
                   "LEFT JOIN products p ON ci.product_id = p.id " +
                   "WHERE ci.id = ?";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(mapResultSetToCartItem(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }

    public CartItem findByUserAndProduct(Long userId, Long productId, Connection conn) {
        if (userId == null || productId == null) return null;
        
        String sql = "SELECT ci.*, p.name as product_name, p.price, p.stock_quantity, p.description, p.image_url, p.category_id, p.created_at as product_created_at, p.updated_at as product_updated_at " +
                   "FROM cart_items ci " +
                   "LEFT JOIN products p ON ci.product_id = p.id " +
                   "WHERE ci.user_id = ? AND ci.product_id = ?";
        
        boolean shouldClose = false;
        if (conn == null) {
            try {
                conn = databaseService.getConnection();
                shouldClose = true;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, productId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCartItem(rs);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (shouldClose && conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return null;
    }
    
    public CartItem findByUserAndProduct(Long userId, Long productId) {
        return findByUserAndProduct(userId, productId, null);
    }
    
    public Optional<CartItem> findByUserAndCartItemId(Long userId, Long cartItemId) {
        if (userId == null || cartItemId == null) return Optional.empty();
        
        String sql = "SELECT ci.*, p.name as product_name, p.price, p.stock_quantity, p.description, p.image_url, p.category_id, p.created_at as product_created_at, p.updated_at as product_updated_at " +
                   "FROM cart_items ci " +
                   "LEFT JOIN products p ON ci.product_id = p.id " +
                   "WHERE ci.user_id = ? AND ci.id = ?";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.setLong(2, cartItemId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(mapResultSetToCartItem(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }

    public CartItem insert(CartItem cartItem, Connection conn) {
        if (cartItem == null || cartItem.getUserId() == null || cartItem.getProduct() == null) {
            return null;
        }
        
        String sql = "INSERT INTO cart_items (user_id, product_id, quantity, added_at) VALUES (?, ?, ?, ?)";
        
        boolean shouldClose = false;
        if (conn == null) {
            try {
                conn = databaseService.getConnection();
                shouldClose = true;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, cartItem.getUserId());
            stmt.setLong(2, cartItem.getProduct().getId());
            stmt.setInt(3, cartItem.getQuantity());
            stmt.setTimestamp(4, Timestamp.valueOf(cartItem.getAddedAt()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                return null;
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    cartItem.setId(generatedKeys.getLong(1));
                    return cartItem;
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (shouldClose && conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return null;
    }
    
    public CartItem insert(CartItem cartItem) {
        return insert(cartItem, null);
    }
    
    public boolean updateQuantity(Long cartItemId, int quantity, Connection conn) {
        if (cartItemId == null || quantity < 0) return false;
        
        String sql = "UPDATE cart_items SET quantity = ? WHERE id = ?";
        
        boolean shouldClose = false;
        if (conn == null) {
            try {
                conn = databaseService.getConnection();
                shouldClose = true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setLong(2, cartItemId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (shouldClose && conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public boolean updateQuantity(Long cartItemId, int quantity) {
        return updateQuantity(cartItemId, quantity, null);
    }
    
    public boolean delete(Long id, Connection conn) {
        if (id == null) return false;
        
        String sql = "DELETE FROM cart_items WHERE id = ?";
        
        boolean shouldClose = false;
        if (conn == null) {
            try {
                conn = databaseService.getConnection();
                shouldClose = true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (shouldClose && conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public boolean delete(Long id) {
        return delete(id, null);
    }
    
    public boolean deleteByUser(Long userId, Connection conn) {
        if (userId == null) return false;
        
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        
        boolean shouldClose = false;
        if (conn == null) {
            try {
                conn = databaseService.getConnection();
                shouldClose = true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            return stmt.executeUpdate() >= 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (shouldClose && conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public boolean deleteByUser(Long userId) {
        return deleteByUser(userId, null);
    }
    
    public boolean deleteByUserId(Long userId) {
        return deleteByUser(userId);
    }

    private CartItem mapResultSetToCartItem(ResultSet rs) throws SQLException {
        if (rs == null) return null;
        
        try {
            CartItem cartItem = new CartItem();
            cartItem.setId(rs.getLong("id"));
            cartItem.setUserId(rs.getLong("user_id"));
            Long productId = rs.getLong("product_id");
            if (!rs.wasNull() && rs.getLong("product_id") > 0) {
                Product product = new Product();
                product.setId(productId);
                product.setName(rs.getString("product_name"));
                product.setPrice(rs.getBigDecimal("price"));
                product.setStockQuantity(rs.getInt("stock_quantity"));
                product.setDescription(rs.getString("description"));
                product.setImageUrl(rs.getString("image_url"));
                product.setCategoryId(rs.getLong("category_id"));
                
                Timestamp productCreatedAt = rs.getTimestamp("product_created_at");
                if (productCreatedAt != null) {
                    product.setCreatedAt(productCreatedAt.toLocalDateTime());
                }
                
                Timestamp productUpdatedAt = rs.getTimestamp("product_updated_at");
                if (productUpdatedAt != null) {
                    product.setUpdatedAt(productUpdatedAt.toLocalDateTime());
                }
                
                cartItem.setProduct(product);
            }
            
            cartItem.setQuantity(rs.getInt("quantity"));
            
            Timestamp addedAt = rs.getTimestamp("added_at");
            if (addedAt != null) {
                cartItem.setAddedAt(addedAt.toLocalDateTime());
            }
            
            return cartItem;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private CartItem mapResultSetToCartItemWithProduct(ResultSet rs) throws SQLException {
        return mapResultSetToCartItem(rs);
    }
}
