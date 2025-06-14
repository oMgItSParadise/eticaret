package source.eticaret.service;

import source.eticaret.model.Order;
import source.eticaret.model.OrderItem;
import source.eticaret.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(DatabaseService databaseService) {
        this.orderRepository = new OrderRepository(databaseService);
    }

    /**
     * Get all orders for a specific seller
     */
    public List<Order> getOrdersBySeller(Long sellerId) {
        return orderRepository.findBySellerId(sellerId);
    }

    /**
     * Get order by ID
     */
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    /**
     * Update order status
     */
    public boolean updateOrderStatus(Long orderId, String newStatus) {
        return orderRepository.updateStatus(orderId, newStatus);
    }

    /**
     * Get total sales amount for a seller
     */
    public BigDecimal getTotalSalesBySeller(Long sellerId) {
        return orderRepository.getTotalSalesBySeller(sellerId);
    }

    /**
     * Get recent orders for a seller
     */
    public List<Order> getRecentOrders(Long sellerId, int limit) {
        return orderRepository.findRecentBySellerId(sellerId, limit);
    }

    /**
     * Get order count by status for a seller
     */
    public long getOrderCountByStatus(Long sellerId, String status) {
        return orderRepository.countBySellerIdAndStatus(sellerId, status);
    }

    /**
     * Get total orders count for a seller
     */
    public long getTotalOrderCount(Long sellerId) {
        return orderRepository.countBySellerId(sellerId);
    }

    /**
     * Get sales statistics for a date range
     */
    public BigDecimal getSalesByDateRange(Long sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.getSalesByDateRange(sellerId, startDate, endDate);
    }
    
    /**
     * Create a new order with order items
     * @param order The order to create
     * @param orderItems List of order items
     * @return The created order with ID, or null if creation failed
     */
    /**
     * Get order items with product details for a seller
     * @param sellerId The ID of the seller
     * @return List of order items with product details
     */
    public List<Map<String, Object>> getOrderItemsBySellerId(Long sellerId) {
        return orderRepository.findOrderItemsBySellerId(sellerId);
    }
    
    public Order createOrder(Order order, List<OrderItem> orderItems) {
        if (order == null || orderItems == null || orderItems.isEmpty()) {
            return null;
        }
        if (order.getStatus() == null || order.getStatus().trim().isEmpty()) {
            order.setStatus("pending");
        }
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDateTime.now());
        }
        
        return orderRepository.createOrder(order, orderItems);
    }
}
