package source.eticaret.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import source.eticaret.model.Product;
import source.eticaret.service.ProductService;

import java.math.BigDecimal;

public class ProductFormController {
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private TextField priceField;
    @FXML private TextField stockField;
    @FXML private TextField categoryIdField;
    @FXML private TextField imageUrlField;
    
    private ProductService productService;
    private Long sellerId;
    private Product product;
    private boolean okClicked = false;
    
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }
    
    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }
    
    public void setProduct(Product product) {
        this.product = product;
        
        if (product != null) {
            nameField.setText(product.getName());
            descriptionField.setText(product.getDescription());
            priceField.setText(product.getPrice().toString());
            stockField.setText(String.valueOf(product.getStockQuantity()));
            categoryIdField.setText(String.valueOf(product.getCategoryId()));
            imageUrlField.setText(product.getImageUrl() != null ? product.getImageUrl() : "");
        }
    }
    
    public boolean isOkClicked() {
        return okClicked;
    }
    public String getName() {
        return nameField.getText().trim();
    }
    
    public String getDescription() {
        return descriptionField.getText().trim();
    }
    
    public BigDecimal getPrice() {
        try {
            return new BigDecimal(priceField.getText().trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
    
    public int getStockQuantity() {
        try {
            return Integer.parseInt(stockField.getText().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public long getCategoryId() {
        try {
            return Long.parseLong(categoryIdField.getText().trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
    
    public String getImageUrl() {
        String url = imageUrlField.getText().trim();
        return url.isEmpty() ? null : url;
    }
    public TextField getNameField() {
        return nameField;
    }
    
    @FXML
    private void handleOk() {
        if (isInputValid()) {
            try {
                if (product == null) {
                    product = new Product();
                }
                
                product.setName(nameField.getText().trim());
                product.setDescription(descriptionField.getText().trim());
                product.setPrice(new BigDecimal(priceField.getText().trim()));
                product.setStockQuantity(Integer.parseInt(stockField.getText().trim()));
                product.setCategoryId(Long.parseLong(categoryIdField.getText().trim()));
                product.setSellerId(sellerId);
                product.setImageUrl(imageUrlField.getText().trim());
                
                okClicked = true;
                closeDialog();
                
            } catch (NumberFormatException e) {
                showAlert("Geçersiz Giriş", "Lütfen geçerli sayısal değerler girin.", 
                         "Fiyat, stok miktarı ve kategori ID sayısal değerler olmalıdır.", AlertType.ERROR);
            } catch (Exception e) {
                showAlert("Hata", "Ürün kaydedilemedi.", 
                         "Ürün kaydedilirken bir hata oluştu: " + e.getMessage(), AlertType.ERROR);
            }
        }
    }
    
    @FXML
    private void handleCancel() {
        closeDialog();
    }
    
    private boolean isInputValid() {
        String errorMessage = "";
        
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            errorMessage += "Ürün adı boş olamaz!\n";
        }
        
        if (priceField.getText() == null || priceField.getText().trim().isEmpty()) {
            errorMessage += "Fiyat boş olamaz!\n";
        } else {
            try {
                BigDecimal price = new BigDecimal(priceField.getText().trim());
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    errorMessage += "Fiyat sıfırdan büyük olmalıdır!\n";
                }
            } catch (NumberFormatException e) {
                errorMessage += "Geçerli bir fiyat giriniz!\n";
            }
        }
        
        if (stockField.getText() == null || stockField.getText().trim().isEmpty()) {
            errorMessage += "Stok miktarı boş olamaz!\n";
        } else {
            try {
                int stock = Integer.parseInt(stockField.getText().trim());
                if (stock < 0) {
                    errorMessage += "Stok miktarı negatif olamaz!\n";
                }
            } catch (NumberFormatException e) {
                errorMessage += "Geçerli bir stok miktarı giriniz!\n";
            }
        }
        
        if (categoryIdField.getText() == null || categoryIdField.getText().trim().isEmpty()) {
            errorMessage += "Kategori ID boş olamaz!\n";
        } else {
            try {
                Long.parseLong(categoryIdField.getText().trim());
            } catch (NumberFormatException e) {
                errorMessage += "Geçerli bir kategori ID giriniz!\n";
            }
        }
        
        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert("Geçersiz Alanlar", "Lütfen geçerli değerler girin", errorMessage, AlertType.ERROR);
            return false;
        }
    }
    
    private void showAlert(String title, String header, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void closeDialog() {
        ((Stage) nameField.getScene().getWindow()).close();
    }
}
