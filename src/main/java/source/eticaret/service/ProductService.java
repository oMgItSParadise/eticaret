package source.eticaret.service;

import source.eticaret.model.Product;
import source.eticaret.repository.ProductRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(DatabaseService databaseService) {
        this.productRepository = new ProductRepository(databaseService);
        this.categoryService = new CategoryService(databaseService);
    }

    /**
     * Create a new product
     */
    /**
     * Add a new product (alias for createProduct for backward compatibility)
     */
    public Product addProduct(Product product) {
        return createProduct(
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStockQuantity(),
            product.getCategoryId(),
            product.getSellerId(),
            product.getImageUrl()
        );
    }
    
    /**
     * Create a new product with individual parameters
     */
    public Product createProduct(String name, String description, BigDecimal price, 
                               int stockQuantity, Long categoryId, Long sellerId, String imageUrl) {

        if (!categoryService.getCategoryById(categoryId).isPresent()) {
            throw new IllegalArgumentException("Invalid category ID");
        }

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        product.setCategoryId(categoryId);
        product.setSellerId(sellerId);
        product.setImageUrl(imageUrl);
        product.setActive(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }

    public Product updateProduct(Long id, String name, String description, BigDecimal price, 
                               int stockQuantity, Long categoryId, String imageUrl, Boolean active) {
        return productRepository.findById(id).map(product -> {
            if (name != null) product.setName(name);
            if (description != null) product.setDescription(description);
            if (price != null) product.setPrice(price);
            if (stockQuantity >= 0) product.setStockQuantity(stockQuantity);
            if (categoryId != null) {
                categoryService.getCategoryById(categoryId).orElseThrow(
                    () -> new IllegalArgumentException("Invalid category ID"));
                product.setCategoryId(categoryId);
            }
            if (imageUrl != null) product.setImageUrl(imageUrl);
            if (active != null) product.setActive(active);
            product.setUpdatedAt(LocalDateTime.now());
            
            return productRepository.save(product);
        }).orElse(null);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByCategory(Long categoryId, int offset, int limit) {
        return productRepository.findByCategoryId(categoryId, offset, limit);
    }

    public List<Product> getProducts(int offset, int limit) {
        return productRepository.findAll(offset, limit);
    }

    public List<Product> searchProducts(String query, int offset, int limit) {
        return productRepository.search(query, offset, limit);
    }

    public List<Product> getProductsBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    public List<Product> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllProducts();
        }
        return productRepository.searchProducts(query.trim());
    }

    public boolean deleteProduct(Long id) {
        return productRepository.delete(id);
    }

    public boolean updateStock(Long productId, int quantityChange) {
        return productRepository.updateStock(productId, quantityChange);
    }

    public boolean isInStock(Long productId, int quantity) {
        return productRepository.findById(productId)
                .map(p -> p.getStockQuantity() >= quantity)
                .orElse(false);
    }

    public boolean decreaseStock(Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        
        return productRepository.findById(productId).map(product -> {
            if (product.getStockQuantity() < quantity) {
                throw new IllegalStateException("Insufficient stock");
            }
            return productRepository.updateStock(productId, -quantity);
        }).orElse(false);
    }

    /**
     * Increase product stock
     */
    public boolean increaseStock(Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        return productRepository.updateStock(productId, quantity);
    }

    /**
     * Toggle product active status
     */
    public boolean toggleProductStatus(Long productId) {
        return productRepository.findById(productId).map(product -> {
            product.setActive(!product.isActive());
            product.setUpdatedAt(LocalDateTime.now());
            return productRepository.save(product) != null;
        }).orElse(false);
    }
}
