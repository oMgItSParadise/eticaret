package source.eticaret.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import source.eticaret.model.Category;
import source.eticaret.model.Product;
import source.eticaret.service.AuthService;
import source.eticaret.service.CategoryService;
import source.eticaret.service.ProductService;
import source.eticaret.Main;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class AddProductController {
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private TextField priceField;
    @FXML private TextField stockField;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private TextField imageUrlField;
    @FXML private CheckBox activeCheckBox;
    
    private ProductService productService;
    private CategoryService categoryService;
    private SellerDashboardController dashboardController;
    
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }
    
    public void setDashboardController(SellerDashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }
    
    @FXML
    public void initialize() {
        this.categoryService = new CategoryService(Main.getDatabaseService());
        loadCategories();
        activeCheckBox.setSelected(true);
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                priceField.setText(oldValue);
            }
        });
        
        stockField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                stockField.setText(oldValue);
            }
        });
    }
    
    private void loadCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            categoryComboBox.setItems(FXCollections.observableArrayList(categories));
            categoryComboBox.setCellFactory(lv -> new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item.getName());
                }
            });
            categoryComboBox.setButtonCell(new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "Kategori seçiniz" : item.getName());
                }
            });
            
        } catch (Exception e) {
            showError("Kategoriler yüklenirken bir hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }
        
        try {
            AuthService authService = AuthService.getInstance(Main.getDatabaseService());
            Long sellerId = authService.getCurrentUser().getId();
            
            Product product = new Product();
            product.setName(nameField.getText().trim());
            product.setDescription(descriptionField.getText().trim());
            product.setPrice(new BigDecimal(priceField.getText().trim()));
            product.setStockQuantity(Integer.parseInt(stockField.getText().trim()));
            product.setCategoryId(categoryComboBox.getValue().getId());
            product.setSellerId(sellerId);
            product.setImageUrl(imageUrlField.getText().trim());
            product.setActive(activeCheckBox.isSelected());
            productService.addProduct(product);
            if (dashboardController != null) {
                dashboardController.refreshProducts();
            }
            showAlert("Başarılı", "Ürün başarıyla eklendi.", Alert.AlertType.INFORMATION);
            closeWindow();
            
        } catch (Exception e) {
            showAlert("Hata", "Ürün eklenirken bir hata oluştu: " + e.getMessage(), 
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleCancel() {
        closeWindow();
    }
    
    @FXML
    private void handleGoToMain() {
        try {
            closeWindow();
            if (dashboardController != null) {
                dashboardController.navigateToDashboard();
            } else {
                System.out.println("Dashboard controller is not set");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Ana sayfaya yönlendirilirken bir hata oluştu.");
        }
    }
    
    private boolean validateInput() {
        if (nameField.getText().trim().isEmpty()) {
            showError("Lütfen ürün adını giriniz.");
            return false;
        }
        
        try {
            new BigDecimal(priceField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Geçerli bir fiyat giriniz.");
            return false;
        }
        
        try {
            Integer.parseInt(stockField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Geçerli bir stok miktarı giriniz.");
            return false;
        }
        
        if (categoryComboBox.getValue() == null) {
            showError("Lütfen bir kategori seçiniz.");
            return false;
        }
        
        return true;
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Hata");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}
