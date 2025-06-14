package source.eticaret.repository;

import source.eticaret.model.Order;
import source.eticaret.model.OrderItem;
import source.eticaret.service.DatabaseService;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class OrderRepository {
    private final DatabaseService databaseService;
    private final ProductRepository productRepository;

    public OrderRepository(DatabaseService databaseService) {
        this.productRepository = new ProductRepository(databaseService);
        this.databaseService = databaseService;
    }

    /**
     * Find orders by seller ID
     */
    public List<Order> findBySellerId(Long sellerId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.username as customer_name " +
                   "FROM orders o " +
                   "JOIN order_items oi ON o.id = oi.order_id " +
                   "JOIN products p ON oi.product_id = p.id " +
                   "JOIN users u ON o.user_id = u.id " +
                   "WHERE p.seller_id = ? " +
                   "GROUP BY o.id " +
                   "ORDER BY o.order_date DESC";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, sellerId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return orders;
    }

    /**
     * Find order by ID
     */
    public Optional<Order> findById(Long id) {
        String sql = "SELECT o.*, u.username as customer_name FROM orders o " +
                   "JOIN users u ON o.user_id = u.id " +
                   "WHERE o.id = ?";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToOrder(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }

    /**
     * Update order status
     */
    public boolean updateStatus(Long orderId, String newStatus) {
        String sql = "UPDATE orders SET status = ?, updated_at = ? WHERE id = ?";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newStatus);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(3, orderId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get total sales amount for a seller
     */
    public BigDecimal getTotalSalesBySeller(Long sellerId) {
        String sql = "SELECT SUM(oi.quantity * oi.unit_price) as total_sales " +
                   "FROM orders o " +
                   "JOIN order_items oi ON o.id = oi.order_id " +
                   "JOIN products p ON oi.product_id = p.id " +
                   "WHERE p.seller_id = ? AND o.status = 'delivered'";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, sellerId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getBigDecimal("total_sales");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return BigDecimal.ZERO;
    }

    /**
     * Find recent orders for a seller
     */
    public List<Order> findRecentBySellerId(Long sellerId, int limit) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.username as customer_name " +
                   "FROM orders o " +
                   "JOIN order_items oi ON o.id = oi.order_id " +
                   "JOIN products p ON oi.product_id = p.id " +
                   "JOIN users u ON o.user_id = u.id " +
                   "WHERE p.seller_id = ? " +
                   "GROUP BY o.id " +
                   "ORDER BY o.order_date DESC " +
                   "LIMIT ?";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, sellerId);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return orders;
    }

    /**
     * Count orders by status for a seller
     */
    public long countBySellerIdAndStatus(Long sellerId, String status) {
        String sql = "SELECT COUNT(DISTINCT o.id) as order_count " +
                   "FROM orders o " +
                   "JOIN order_items oi ON o.id = oi.order_id " +
                   "JOIN products p ON oi.product_id = p.id " +
                   "WHERE p.seller_id = ? AND o.status = ?";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, sellerId);
            stmt.setString(2, status);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getLong("order_count");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }

    /**
     * Count total orders for a seller
     */
    public long countBySellerId(Long sellerId) {
        String sql = "SELECT COUNT(DISTINCT o.id) as order_count " +
                   "FROM orders o " +
                   "JOIN order_items oi ON o.id = oi.order_id " +
                   "JOIN products p ON oi.product_id = p.id " +
                   "WHERE p.seller_id = ?";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, sellerId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getLong("order_count");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Create a new order and order items
     * @param order The order to create
     * @param orderItems List of order items
     * @return The created order with ID, or null if creation failed
     */
    public Order createOrder(Order order, List<OrderItem> orderItems) {
        String orderSql = "INSERT INTO orders (user_id, total_amount, status, shipping_address, payment_method) " +
                        "VALUES (?, ?, ?, ?, ?)";
        
        String itemSql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) " +
                        "VALUES (?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = databaseService.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement orderStmt = conn.prepareStatement(
                    orderSql, Statement.RETURN_GENERATED_KEYS)) {
                
                orderStmt.setLong(1, order.getUserId());
                orderStmt.setBigDecimal(2, order.getTotalAmount());
                orderStmt.setString(3, order.getStatus());
                orderStmt.setString(4, order.getShippingAddress());
                orderStmt.setString(5, order.getPaymentMethod());
                
                int affectedRows = orderStmt.executeUpdate();
                
                if (affectedRows == 0) {
                    throw new SQLException("Sipariş oluşturulamadı, hiçbir satır etkilenmedi.");
                }
                
                try (ResultSet generatedKeys = orderStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        order.setId(generatedKeys.getLong(1));
                    } else {
                        throw new SQLException("Sipariş oluşturulamadı, ID alınamadı.");
                    }
                }
            }
            try (PreparedStatement itemStmt = conn.prepareStatement(itemSql)) {
                for (OrderItem item : orderItems) {
                    itemStmt.setLong(1, order.getId());
                    itemStmt.setLong(2, item.getProductId());
                    itemStmt.setInt(3, item.getQuantity());
                    itemStmt.setBigDecimal(4, item.getUnitPrice());
                    itemStmt.addBatch();
                }
                
                int[] results = itemStmt.executeBatch();
                for (int result : results) {
                    if (result == Statement.EXECUTE_FAILED) {
                        throw new SQLException("Sipariş detayları kaydedilemedi.");
                    }
                }
            }
            
            conn.commit();
            return order;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Get sales by date range for a seller
     */
    public BigDecimal getSalesByDateRange(Long sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = "SELECT SUM(oi.quantity * oi.unit_price) as total_sales " +
                   "FROM orders o " +
                   "JOIN order_items oi ON o.id = oi.order_id " +
                   "JOIN products p ON oi.product_id = p.id " +
                   "WHERE p.seller_id = ? AND o.order_date BETWEEN ? AND ? AND o.status = 'delivered'";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, sellerId);
            stmt.setTimestamp(2, Timestamp.valueOf(startDate));
            stmt.setTimestamp(3, Timestamp.valueOf(endDate));
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                BigDecimal result = rs.getBigDecimal("total_sales");
                return result != null ? result : BigDecimal.ZERO;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return BigDecimal.ZERO;
    }

    /**
     * Find order items with product details by seller ID
     */
    public List<Map<String, Object>> findOrderItemsBySellerId(Long sellerId) {
        List<Map<String, Object>> orderItems = new ArrayList<>();
        String sql = "SELECT oi.*, p.name as product_name, p.image_url, o.order_date, o.status as order_status, " +
                   "u.username as customer_name, oi.quantity * oi.unit_price as item_total " +
                   "FROM order_items oi " +
                   "JOIN products p ON oi.product_id = p.id " +
                   "JOIN orders o ON oi.order_id = o.id " +
                   "JOIN users u ON o.user_id = u.id " +
                   "WHERE p.seller_id = ? " +
                   "ORDER BY o.order_date DESC";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, sellerId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", rs.getLong("id"));
                item.put("orderId", rs.getLong("order_id"));
                item.put("productId", rs.getLong("product_id"));
                item.put("productName", rs.getString("product_name"));
                item.put("imageUrl", rs.getString("image_url"));
                item.put("quantity", rs.getInt("quantity"));
                item.put("unitPrice", rs.getBigDecimal("unit_price"));
                item.put("itemTotal", rs.getBigDecimal("item_total"));
                item.put("orderDate", rs.getTimestamp("order_date").toLocalDateTime());
                item.put("orderStatus", rs.getString("order_status"));
                item.put("customerName", rs.getString("customer_name"));
                
                orderItems.add(item);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return orderItems;
    }
    
    /**
     * Map ResultSet to Order object
     */
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        order.setUserId(rs.getLong("user_id"));
        order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setStatus(rs.getString("status"));
        order.setShippingAddress(rs.getString("shipping_address"));
        order.setPaymentMethod(rs.getString("payment_method"));
        
        try {
            order.setCustomerName(rs.getString("customer_name"));
        } catch (SQLException e) {
        }
        
        return order;
    }
}
