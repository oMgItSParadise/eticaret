package source.eticaret.service;

import source.eticaret.model.CartItem;
import source.eticaret.model.Order;
import source.eticaret.model.OrderItem;
import source.eticaret.model.Product;
import source.eticaret.model.User;
import source.eticaret.repository.CartRepository;
import source.eticaret.repository.ProductRepository;
import source.eticaret.service.DatabaseService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CartService {
    private User currentUser;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final AuthService authService;
    private final ProductService productService;
    private final OrderService orderService;
    private final DatabaseService databaseService;
    
    public CartService(DatabaseService databaseService) {
        this.databaseService = databaseService;
        this.cartRepository = new CartRepository(databaseService);
        this.productRepository = new ProductRepository(databaseService);
        this.authService = AuthService.getInstance(databaseService);
        this.productService = new ProductService(databaseService);
        this.orderService = new OrderService(databaseService);
        this.currentUser = authService.getCurrentUser();
    }

    public List<CartItem> getCartItems() {
        if (currentUser == null) return new ArrayList<>();
        return cartRepository.findByUserId(currentUser.getId());
    }
    
    public List<CartItem> getCartItems(Long userId) {
        if (userId == null) return new ArrayList<>();
        return cartRepository.findByUserId(userId);
    }

    public boolean addToCart(Long productId, int quantity) {
        if (currentUser == null || productId == null || quantity <= 0) {
            return false;
        }
        
        try (Connection conn = databaseService.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                Product product = productRepository.findByIdWithLock(productId, conn)
                    .orElseThrow(() -> new IllegalStateException("Ürün bulunamadı"));
                
                if (product.getStockQuantity() < quantity) {
                    conn.rollback();
                    return false;
                }
                CartItem existingItem = cartRepository.findByUserAndProduct(currentUser.getId(), productId, conn);
                
                if (existingItem != null) {
                    int newQuantity = existingItem.getQuantity() + quantity;
                    if (product.getStockQuantity() < newQuantity) {
                        conn.rollback();
                        return false;
                    }
                    
                    existingItem.setQuantity(newQuantity);
                    boolean updated = cartRepository.updateQuantity(existingItem.getId(), newQuantity, conn);
                    if (updated) {
                        conn.commit();
                        return true;
                    }
                } else {
                    if (product.getStockQuantity() < quantity) {
                        conn.rollback();
                        return false;
                    }
                    
                    CartItem newItem = new CartItem();
                    newItem.setUserId(currentUser.getId());
                    newItem.setProduct(product);
                    newItem.setQuantity(quantity);
                    newItem.setAddedAt(LocalDateTime.now());
                    
                    CartItem inserted = cartRepository.insert(newItem, conn);
                    if (inserted != null) {
                        conn.commit();
                        return true;
                    }
                }
                conn.rollback();
                return false;
                
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateQuantity(Long cartItemId, int newQuantity) {
        if (cartItemId == null || newQuantity < 0) return false;
        
        try {
            Optional<CartItem> itemOpt = cartRepository.findById(cartItemId);
            if (itemOpt.isEmpty() || currentUser == null || 
                !itemOpt.get().getUserId().equals(currentUser.getId())) {
                return false;
            }
            
            CartItem item = itemOpt.get();
            if (newQuantity == 0) {
                return removeFromCart(cartItemId);
            }
            Optional<Product> productOpt = productService.getProductById(item.getProduct().getId());
            if (productOpt.isEmpty() || productOpt.get().getStockQuantity() < newQuantity) {
                return false;
            }
            
            item.setQuantity(newQuantity);
            return cartRepository.updateQuantity(cartItemId, newQuantity);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeFromCart(Long cartItemId) {
        if (cartItemId == null || currentUser == null) return false;
        
        try {
            Optional<CartItem> itemOpt = cartRepository.findById(cartItemId);
            if (itemOpt.isPresent() && itemOpt.get().getUserId().equals(currentUser.getId())) {
                return cartRepository.delete(cartItemId);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void clearCart() {
        if (currentUser != null) {
            cartRepository.deleteByUserId(currentUser.getId());
        }
    }
    
    /**
     * Processes the checkout for the current user's cart.
     * @throws Exception if there's an error during checkout
     */
    public void checkout() throws Exception {
        if (currentUser == null) {
            throw new IllegalStateException("Kullanıcı girişi yapılmamış.");
        }
        
        List<CartItem> cartItems = getCartItems(currentUser.getId());
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Sepetiniz boş.");
        }
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            Product product = productService.getProductById(item.getProduct().getId())
                .orElseThrow(() -> new IllegalStateException("Ürün bulunamadı: " + item.getProduct().getId()));
                
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new IllegalStateException(String.format(
                    "Yetersiz stok: %s (Stok: %d, İstenen: %d)", 
                    product.getName(), 
                    product.getStockQuantity(),
                    item.getQuantity()
                ));
            }
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        Order order = new Order();
        order.setUserId(currentUser.getId());
        order.setTotalAmount(totalAmount);
        order.setStatus("pending");
        order.setOrderDate(LocalDateTime.now());
        order.setPaymentMethod("credit_card");
        order.setShippingAddress("");
        Order createdOrder = orderService.createOrder(order, orderItems);
        if (createdOrder == null) {
            throw new Exception("Sipariş oluşturulurken bir hata oluştu.");
        }
        for (CartItem item : cartItems) {
            Product product = productService.getProductById(item.getProduct().getId()).get();
            int newStock = product.getStockQuantity() - item.getQuantity();
            productService.updateProduct(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                newStock,
                product.getCategoryId(),
                product.getImageUrl(),
                product.isActive()
            );
        }
        clearCart();
    }
    
    public User getCurrentUser() {
        return currentUser;
    }

    public BigDecimal getCartTotal() {
        if (currentUser == null) return BigDecimal.ZERO;
        
        List<CartItem> items = getCartItems(currentUser.getId());
        BigDecimal total = BigDecimal.ZERO;
        
        for (CartItem item : items) {
            if (item != null && item.getProduct() != null) {
                BigDecimal price = item.getProduct().getPrice();
                if (price != null) {
                    total = total.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));
                }
            }
        }
        
        return total;
    }

    public int getCartItemCount() {
        if (currentUser == null) return 0;
        
        try {
            return cartRepository.countByUserId(currentUser.getId());
        } catch (Exception e) {
            e.printStackTrace();
            try {
                List<CartItem> items = getCartItems(currentUser.getId());
                return items.stream()
                    .filter(Objects::nonNull)
                    .mapToInt(CartItem::getQuantity)
                    .sum();
            } catch (Exception ex) {
                ex.printStackTrace();
                return 0;
            }
        }
    }
}
