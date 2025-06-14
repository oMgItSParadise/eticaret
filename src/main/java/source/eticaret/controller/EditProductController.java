package source.eticaret.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import source.eticaret.model.Product;
import source.eticaret.service.ProductService;
import java.math.BigDecimal;

public class EditProductController {
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private TextField priceField;
    @FXML private TextField stockField;
    @FXML private CheckBox activeCheckBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Product product;
    private ProductService productService;
    private SellerDashboardController dashboardController;

    public void setProduct(Product product) {
        this.product = product;
        populateFields();
    }

    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    public void setDashboardController(SellerDashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML
    public void initialize() {
        saveButton.setOnAction(event -> handleSave());
        cancelButton.setOnAction(event -> closeWindow());
    }

    private void populateFields() {
        if (product != null) {
            nameField.setText(product.getName());
            descriptionField.setText(product.getDescription());
            priceField.setText(String.format("%.2f", product.getPrice()));
            stockField.setText(String.valueOf(product.getStockQuantity()));
            activeCheckBox.setSelected(product.isActive());
        }
    }

    private void handleSave() {
        try {
            String name = nameField.getText().trim();
            String description = descriptionField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());
            int stock = Integer.parseInt(stockField.getText().trim());
            boolean isActive = activeCheckBox.isSelected();

            if (name.isEmpty() || description.isEmpty() || price <= 0 || stock < 0) {
                showError("Lütfen tüm alanları doğru bir şekilde doldurun.");
                return;
            }
            Product updatedProduct = productService.updateProduct(
                product.getId(),
                name,
                description,
                BigDecimal.valueOf(price),
                stock,
                product.getCategoryId(),
                product.getImageUrl(),
                isActive
            );
            
            if (updatedProduct == null) {
                throw new RuntimeException("Ürün güncellenirken bir hata oluştu.");
            }
            if (updatedProduct != null) {
                showSuccess("Ürün başarıyla güncellendi.");
                if (dashboardController != null) {
                    dashboardController.refreshProducts();
                }
                closeWindow();
            } else {
                showError("Ürün güncellenirken bir hata oluştu.");
            }
        } catch (NumberFormatException e) {
            showError("Lütfen geçerli sayısal değerler girin.");
        } catch (Exception e) {
            showError("Bir hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Hata");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Başarılı");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
