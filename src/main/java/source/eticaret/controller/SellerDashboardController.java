package source.eticaret.controller;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import source.eticaret.model.Order;
import source.eticaret.model.Product;
import source.eticaret.model.User;
import source.eticaret.service.DatabaseService;
import source.eticaret.service.OrderService;
import source.eticaret.service.ProductService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.stage.Modality;

public class SellerDashboardController {
    private User currentUser;
    private ProductService productService;
    private OrderService orderService;
    private List<Product> allProducts = new ArrayList<>();

    @FXML private TextField searchField;
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, Long> productIdCol;
    @FXML private TableColumn<Product, String> productNameCol;
    @FXML private TableColumn<Product, String> productPriceCol;
    @FXML private TableColumn<Product, Integer> productStockCol;
    @FXML private TableColumn<Product, String> productStatusCol;
    @FXML private TableColumn<Product, Void> productActionsCol;
    
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, String> orderIdCol;
    @FXML private TableColumn<Order, String> orderDateCol;
    @FXML private TableColumn<Order, String> orderCustomerCol;
    @FXML private TableColumn<Order, String> orderAmountCol;
    @FXML private TableColumn<Order, String> orderStatusCol;
    @FXML private TableColumn<Order, Void> orderActionsCol;
    @FXML private TableView<Map<String, Object>> orderItemsTable;
    @FXML private TableColumn<Map<String, Object>, String> orderItemProductCol;
    @FXML private TableColumn<Map<String, Object>, Number> orderItemQuantityCol;
    @FXML private TableColumn<Map<String, Object>, Number> orderItemPriceCol;
    @FXML private TableColumn<Map<String, Object>, Number> orderItemTotalCol;
    @FXML private TableColumn<Map<String, Object>, String> orderItemStatusCol;
    
    @FXML private Label statusLabel;
    @FXML private Label pageInfoLabel;
    
    @FXML private Button previousButton;
    @FXML private Button nextButton;
    
    public void setUser(User user) {
        this.currentUser = user;
        if (productService == null || orderService == null) {
            initializeServices();
        }
        if (productsTable != null) {
            loadProducts();
            loadOrders();
        }
    }
    
    /**
     * Navigates back to the dashboard view.
     * This method is called when the user clicks the back button from other views.
     */
    public void navigateToDashboard() {
        try {
            Stage stage = (Stage) productsTable.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/templates/seller/dashboard.fxml"));
            Parent root = loader.load();
            SellerDashboardController controller = loader.getController();
            controller.setUser(currentUser);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Satıcı Paneli - " + currentUser.getUsername());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Hata", "Pencereler arasında geçiş yapılırken bir hata oluştu: " + e.getMessage(), 
                     Alert.AlertType.ERROR);
        }
    }

    private void initializeServices() {
        DatabaseService databaseService = DatabaseService.getInstance();
        this.productService = new ProductService(databaseService);
        this.orderService = new OrderService(databaseService);
    }

    @FXML
    private void initialize() {
        setupProductsTable();
        setupOrdersTable();
        if (previousButton != null) previousButton.setVisible(false);
        if (nextButton != null) nextButton.setVisible(false);
        if (pageInfoLabel != null) pageInfoLabel.setVisible(false);
        if (productService == null || orderService == null) {
            initializeServices();
        }
        if (currentUser != null) {
            loadProducts();
            loadOrders();
        }
    }

    private void setupProductsTable() {
        productIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        productNameCol.setCellFactory(column -> new TableCell<>() {
            private final HBox container = new HBox(5);
            private final Label nameLabel = new Label();
            private final Button editButton = new Button("Düzenle");
            private final Button deleteButton = new Button("Sil");
            
            {
                container.setAlignment(Pos.CENTER_LEFT);
                editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 2 8;");
                deleteButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-padding: 2 8;");
                
                editButton.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    handleEditProduct(product);
                });
                
                deleteButton.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    handleDeleteProduct(product);
                });
                
                container.getChildren().addAll(nameLabel, editButton, deleteButton);
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    nameLabel.setText(item);
                    setGraphic(container);
                }
            }
        });
        
        productNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        productPriceCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.format("%.2f TL", cellData.getValue().getPrice())));
        productStockCol.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        productStatusCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().isActive() ? "Aktif" : "Pasif"));
    }
    
    private void handleDeleteProduct(Product product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Ürün Silme");
        alert.setHeaderText("Ürün Silinecek");
        alert.setContentText("\"" + product.getName() + "\" adlı ürünü silmek istediğinizden emin misiniz?\nBu işlem geri alınamaz!");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean deleted = productService.deleteProduct(product.getId());
                    if (deleted) {
                        showAlert("Başarılı", "Ürün başarıyla silindi.", Alert.AlertType.INFORMATION);
                        loadProducts();
                    } else {
                        showAlert("Hata", "Ürün silinirken bir hata oluştu.", Alert.AlertType.ERROR);
                    }
                } catch (Exception e) {
                    showAlert("Hata", "Ürün silinirken bir hata oluştu: " + e.getMessage(), Alert.AlertType.ERROR);
                    e.printStackTrace();
                }
            }
        });
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupOrdersTable() {
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        orderDateCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getOrderDate().toString()));
        orderCustomerCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCustomerName()));
        orderAmountCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.format("%.2f TL", cellData.getValue().getTotalAmount())));
        orderStatusCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(translateStatus(cellData.getValue().getStatus())));
        orderItemProductCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().get("productName").toString()));
        orderItemQuantityCol.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>((Number)cellData.getValue().get("quantity")));
        orderItemPriceCol.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>((Number)cellData.getValue().get("unitPrice")));
        orderItemTotalCol.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>((Number)cellData.getValue().get("itemTotal")));
        orderItemStatusCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(translateStatus(cellData.getValue().get("orderStatus").toString())));
        orderItemPriceCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Number price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f TL", price.doubleValue()));
                }
            }
        });
        
        orderItemTotalCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Number total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f TL", total.doubleValue()));
                }
            }
        });
        ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadOrderItems(newSelection.getId());
            }
        });
    }

    private void loadProducts() {
        try {
            if (currentUser == null) {
                showError("Kullanıcı bilgisi alınamadı. Lütfen tekrar giriş yapın.");
                return;
            }
            allProducts.clear();
            List<Product> products = productService.getProductsBySeller(currentUser.getId());
            
            if (products != null) {
                allProducts.addAll(products);
                allProducts.sort((p1, p2) -> p1.getId().compareTo(p2.getId()));
                productsTable.getItems().setAll(allProducts);
                showInfo(allProducts.size() + " ürün listelendi.");
            } else {
                showError("Ürünler yüklenirken bir hata oluştu.");
            }
            
        } catch (Exception e) {
            showError("Ürünler yüklenirken bir hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Public method to refresh the products table.
     * Can be called from other controllers.
     */
    public void refreshProducts() {
        loadProducts();
    }

    private void loadOrders() {
        if (currentUser == null) {
            showError("Kullanıcı bilgileri yüklenemedi. Lütfen tekrar giriş yapın.");
            return;
        }
        
        try {
            List<Order> orders = orderService.getOrdersBySeller(currentUser.getId());
            if (orders != null && !orders.isEmpty()) {
                ordersTable.getItems().setAll(orders);
                showInfo(orders.size() + " sipariş listelendi.");
                if (!orders.isEmpty()) {
                    ordersTable.getSelectionModel().selectFirst();
                    loadOrderItems(orders.get(0).getId());
                }
            } else {
                ordersTable.getItems().clear();
                orderItemsTable.getItems().clear();
                showInfo("Henüz sipariş bulunmamaktadır.");
            }
        } catch (Exception e) {
            showError("Siparişler yüklenirken bir hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadOrderItems(Long orderId) {
        if (currentUser == null || orderId == null) {
            return;
        }
        
        try {
            orderItemsTable.getItems().clear();
            List<Map<String, Object>> orderItems = orderService.getOrderItemsBySellerId(currentUser.getId())
                .stream()
                .filter(item -> orderId.equals(item.get("orderId")))
                .collect(Collectors.toList());
                
            if (!orderItems.isEmpty()) {
                orderItemsTable.getItems().setAll(orderItems);
            }
        } catch (Exception e) {
            showError("Sipariş detayları yüklenirken bir hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddProduct() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/templates/seller/add_product.fxml"));
            Stage stage = new Stage();
            VBox root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/dialogs.css").toExternalForm());
            
            stage.setTitle("Yeni Ürün Ekle");
            stage.setScene(scene);
            stage.setResizable(false);
            AddProductController controller = loader.getController();
            controller.setProductService(productService);
            controller.setDashboardController(this);
            stage.show();
            
        } catch (IOException e) {
            showError("Ürün ekleme sayfası yüklenirken bir hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditProduct(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/templates/seller/edit_product.fxml"));
            Parent root = loader.load();
            
            EditProductController controller = loader.getController();
            controller.setProduct(product);
            controller.setProductService(productService);
            controller.setDashboardController(this);
            
            Stage stage = new Stage();
            stage.setTitle("Ürün Düzenle");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
        } catch (IOException e) {
            showError("Ürün düzenleme sayfası yüklenirken bir hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewOrder(Order order) {
    }

    @FXML
    private void handleUpdateOrderStatus(Order order) {
    }

    @FXML
    private void handleToggleProductStatus(Product product) {
    }
    
    @FXML
    private void handleSearch() {
        try {
            if (currentUser == null) {
                showError("Kullanıcı bilgileri yüklenemedi. Lütfen tekrar giriş yapın.");
                return;
            }
            
            String searchText = searchField.getText().trim().toLowerCase();
            
            if (searchText.isEmpty()) {
                loadProducts();
                return;
            }
            List<Product> filteredProducts = allProducts.stream()
                .filter(p -> String.valueOf(p.getId()).contains(searchText) || 
                           p.getName().toLowerCase().contains(searchText))
                .collect(Collectors.toList());
                
            productsTable.getItems().setAll(filteredProducts);
            showInfo(filteredProducts.size() + " ürün bulundu.");
                
        } catch (Exception e) {
            showError("Arama yapılırken bir hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleBackToShopping() {
        try {
            Stage stage = (Stage) productsTable.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/templates/home/index.fxml"));
            Parent root = loader.load();
            HomeController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("E-Ticaret Uygulaması");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Ana sayfaya dönülürken bir hata oluştu: " + e.getMessage());
        }
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) return "0.00 TL";
        return String.format("%.2f TL", price);
    }

    private String translateStatus(String status) {
        if (status == null) return "Bilinmeyen";
        switch (status.toLowerCase()) {
            case "pending": return "Beklemede";
            case "processing": return "İşleniyor";
            case "shipped": return "Kargoda";
            case "delivered": return "Teslim Edildi";
            case "cancelled": return "İptal Edildi";
            default: return status;
        }
    }

    private void showInfo(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setStyle("-fx-text-fill: green;");
        }
    }

    private void showError(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
}
