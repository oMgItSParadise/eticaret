package source.eticaret.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import source.eticaret.model.Category;
import source.eticaret.model.Product;
import source.eticaret.service.CategoryService;
import source.eticaret.service.DatabaseService;
import source.eticaret.service.ProductService;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProductController {
    private final ProductService productService;
    private final CategoryService categoryService;

    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Long> idColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, BigDecimal> priceColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, Boolean> activeColumn;
    
    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField priceField;
    @FXML private TextField stockField;
    @FXML private ComboBox<Category> categoryCombo;
    @FXML private TextField imageUrlField;
    @FXML private CheckBox activeCheckbox;
    @FXML private Button browseButton;
    @FXML private Button saveButton;
    @FXML private Button deleteButton;
    @FXML private VBox formContainer;
    @FXML private TextField searchField;

    private Product currentProduct;
    private Long currentSellerId;

    public ProductController() {
        DatabaseService dbService = DatabaseService.getInstance();
        this.productService = new ProductService(dbService);
        this.categoryService = new CategoryService(dbService);
    }

    public void setCurrentSeller(Long sellerId) {
        this.currentSellerId = sellerId;
        loadProducts();
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        loadCategories();
        setupFormValidation();
        clearForm();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        

        categoryColumn.setCellValueFactory(cellData -> {
            Long categoryId = cellData.getValue().getCategoryId();
            if (categoryId != null) {
                Optional<Category> category = categoryService.getCategoryById(categoryId);
                return new javafx.beans.property.SimpleStringProperty(
                    category.map(Category::getName).orElse("Bilinmeyen Kategori")
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });


        productTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    loadProductDetails(newSelection);
                }
            });
    }

    private void loadProducts() {
        if (currentSellerId != null) {
            List<Product> products = productService.getProductsBySeller(currentSellerId);
            productTable.getItems().setAll(products);
        }
    }

    private void loadCategories() {
        List<Category> categories = categoryService.getAllCategories();
        categoryCombo.getItems().clear();
        categoryCombo.getItems().addAll(categories);
    }

    private void loadProductDetails(Product product) {
        currentProduct = product;
        nameField.setText(product.getName());
        descriptionArea.setText(product.getDescription());
        priceField.setText(product.getPrice().toString());
        stockField.setText(String.valueOf(product.getStockQuantity()));
        imageUrlField.setText(product.getImageUrl());
        activeCheckbox.setSelected(product.isActive());


        if (product.getCategoryId() != null) {
            Optional<Category> category = categoryService.getCategoryById(product.getCategoryId());
            category.ifPresent(categoryCombo::setValue);
        } else {
            categoryCombo.setValue(null);
        }

        saveButton.setText("Güncelle");
        deleteButton.setDisable(false);
        formContainer.setDisable(false);
    }

    private void setupFormValidation() {

        saveButton.disableProperty().bind(
            nameField.textProperty().isEmpty()
                .or(priceField.textProperty().isEmpty())
                .or(stockField.textProperty().isEmpty())
                .or(categoryCombo.valueProperty().isNull())
        );
    }

    @FXML
    private void handleSave() {
        try {
            String name = nameField.getText();
            String description = descriptionArea.getText();
            BigDecimal price = new BigDecimal(priceField.getText());
            int stock = Integer.parseInt(stockField.getText());
            Long categoryId = categoryCombo.getValue().getId();
            String imageUrl = imageUrlField.getText();
            boolean active = activeCheckbox.isSelected();

            if (currentProduct == null) {

                Product newProduct = productService.createProduct(
                    name, description, price, stock, 
                    categoryId, currentSellerId, imageUrl
                );
                
                if (newProduct != null) {
                    showAlert("Başarılı", "Ürün başarıyla oluşturuldu.", Alert.AlertType.INFORMATION);
                    clearForm();
                    loadProducts();
                } else {
                    showAlert("Hata", "Ürün oluşturulurken bir hata oluştu.", Alert.AlertType.ERROR);
                }
            } else {

                Product updatedProduct = productService.updateProduct(
                    currentProduct.getId(),
                    name,
                    description,
                    price,
                    stock,
                    categoryId,
                    imageUrl,
                    active
                );
                
                if (updatedProduct != null) {
                    showAlert("Başarılı", "Ürün başarıyla güncellendi.", Alert.AlertType.INFORMATION);
                    clearForm();
                    loadProducts();
                } else {
                    showAlert("Hata", "Ürün güncellenirken bir hata oluştu.", Alert.AlertType.ERROR);
                }
            }
        } catch (NumberFormatException e) {
            showAlert("Geçersiz Giriş", "Lütfen geçerli sayısal değerler girin.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Hata", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDelete() {
        if (currentProduct != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Ürün Sil");
            alert.setHeaderText("Ürün Silinecek");
            alert.setContentText("Bu ürünü silmek istediğinizden emin misiniz?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (productService.deleteProduct(currentProduct.getId())) {
                    showAlert("Bilgi", "Lütfen düzenlemek istediğiniz ürünü seçin.", Alert.AlertType.INFORMATION);
                    clearForm();
                    loadProducts();
                } else {
                    showAlert("Hata", "Ürün silinirken bir hata oluştu.", Alert.AlertType.ERROR);
                }
            }
        }
    }

    @FXML
    private void handleNewProduct() {
        currentProduct = null;
        clearForm();
        formContainer.setDisable(false);
        nameField.requestFocus();
    }

    @FXML
    private void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Ürün Resmi Seç");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Resim Dosyaları", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        Stage stage = (Stage) browseButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        
        if (file != null) {

            imageUrlField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (!query.isEmpty()) {
            List<Product> results = productService.searchProducts(query);
            productTable.getItems().setAll(results);
        } else {
            loadProducts();
        }
    }

    private void clearForm() {
        currentProduct = null;
        nameField.clear();
        descriptionArea.clear();
        priceField.clear();
        stockField.clear();
        categoryCombo.setValue(null);
        imageUrlField.clear();
        activeCheckbox.setSelected(true);
        saveButton.setText("Kaydet");
        deleteButton.setDisable(true);
        formContainer.setDisable(true);
        productTable.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
