package source.eticaret.model;

import javafx.beans.property.SimpleDoubleProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CartItem {
    private Long id;
    private Long userId;
    private Product product;
    private int quantity;
    private LocalDateTime addedAt;
    public CartItem() {
        this.addedAt = LocalDateTime.now();
    }

    public CartItem(Long id, Long userId, Product product, int quantity, LocalDateTime addedAt) {
        this.id = id;
        this.userId = userId;
        this.product = product;
        this.quantity = quantity;
        this.addedAt = addedAt != null ? addedAt : LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
    public BigDecimal getTotalPrice() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
    
    public SimpleDoubleProperty priceProperty() {
        return new SimpleDoubleProperty(product.getPrice().doubleValue());
    }
    
    public SimpleDoubleProperty totalPriceProperty() {
        return new SimpleDoubleProperty(getTotalPrice().doubleValue());
    }
}
