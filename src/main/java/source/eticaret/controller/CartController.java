package source.eticaret.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import source.eticaret.Main;
import source.eticaret.model.CartItem;
import source.eticaret.model.Product;
import source.eticaret.model.User;
import source.eticaret.service.CartService;
import source.eticaret.service.DatabaseService;
import source.eticaret.view.ViewManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static source.eticaret.view.ViewManager.showAlert;

public class CartController {
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> productNameCol;
    @FXML private TableColumn<CartItem, String> priceCol;
    @FXML private TableColumn<CartItem, Integer> quantityCol;
    @FXML private TableColumn<CartItem, String> totalCol;
    @FXML private Label totalLabel;
    @FXML private VBox cartContent;
    @FXML private Button checkoutButton;
    @FXML private Button continueShoppingButton;
    @FXML private Button updateCartButton;
    @FXML private Button clearCartButton;

    private CartService cartService;
    private User currentUser;

    public CartController() {
        DatabaseService databaseService = Main.getDatabaseService();
        cartService = new CartService(databaseService);
    }

    @FXML
    public void setUser(User user) {
        this.currentUser = user;
        cartService.setCurrentUser(user);
        loadCartItems();
    }
    public void setCurrentUser(User user) {
        setUser(user);
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        loadCartItems();
        updateCartButton.setOnAction(e -> updateCart());
        clearCartButton.setOnAction(e -> handleRemoveAll());
        checkoutButton.setOnAction(e -> handleCheckout());
        continueShoppingButton.setOnAction(e -> continueShopping());
    }

    private void setupTableColumns() {
        productNameCol.setCellValueFactory(cellData -> {
            Product product = cellData.getValue().getProduct();
            return new SimpleStringProperty(product != null ? product.getName() : "");
        });
        
        priceCol.setCellValueFactory(cellData -> {
            Product product = cellData.getValue().getProduct();
            return new SimpleStringProperty(product != null ? 
                String.format("%.2f TL", product.getPrice()) : "0.00 TL");
        });
        
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        
        totalCol.setCellValueFactory(cellData -> {
            CartItem item = cellData.getValue();
            if (item.getProduct() != null) {
                BigDecimal price = item.getProduct().getPrice();
                BigDecimal total = price.multiply(BigDecimal.valueOf(item.getQuantity()));
                return new SimpleStringProperty(String.format("%.2f TL", total));
            }
            return new SimpleStringProperty("0.00 TL");
        });
        quantityCol.setCellFactory(column -> new TableCell<>() {
            private final Spinner<Integer> spinner = new Spinner<>(1, 100, 1);
            
            {
                spinner.setEditable(true);
                spinner.focusedProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal) {
                        commitEdit(spinner.getValue());
                    }
                });
                spinner.getEditor().setOnAction(event -> {
                    commitEdit(spinner.getValue());
                    event.consume();
                });
            }

            @Override
            public void commitEdit(Integer newValue) {
                if (!isEditing() && newValue == null) {
                    return;
                }
                final int index = getIndex();
                final List<CartItem> items = getTableView().getItems();
                if (index < 0 || index >= items.size()) {
                    cancelEdit();
                    return;
                }
                
                CartItem cartItem = items.get(index);
                if (cartItem == null) {
                    cancelEdit();
                    return;
                }
                
                super.commitEdit(newValue);
                
                if (newValue != null && newValue != cartItem.getQuantity()) {
                    try {
                        boolean success = cartService.updateQuantity(cartItem.getId(), newValue);
                        if (success) {
                            loadCartItems();
                            updateTotal();
                        } else {
                            spinner.getValueFactory().setValue(cartItem.getQuantity());
                            ViewManager.showErrorAlert("Hata", "Stok yetersiz veya bir hata oluştu.");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        spinner.getValueFactory().setValue(cartItem.getQuantity());
                        ViewManager.showErrorAlert("Hata", "Miktar güncellenirken bir hata oluştu: " + e.getMessage());
                    }
                }
            }

            
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                
                try {
                    List<CartItem> items = getTableView() != null ? getTableView().getItems() : null;
                    int index = getIndex();
                    if (items == null || index < 0 || index >= items.size()) {
                        setGraphic(null);
                        return;
                    }
                    CartItem cartItem = items.get(index);
                    if (cartItem == null) {
                        setGraphic(null);
                        return;
                    }
                    if (spinner != null && spinner.getValueFactory() != null) {
                        int currentSpinnerValue = spinner.getValue() != null ? spinner.getValue() : 0;
                        if (cartItem.getQuantity() != currentSpinnerValue) {
                            spinner.getValueFactory().setValue(cartItem.getQuantity());
                        }
                    }
                    
                    setGraphic(spinner);
                } catch (Exception e) {
                    e.printStackTrace();
                    setGraphic(null);
                }
            }
        });
        TableColumn<CartItem, Void> removeCol = new TableColumn<>("İşlem");
        removeCol.setCellFactory(param -> new TableCell<>() {
            private final Button removeButton = new Button("Kaldır");
            
            {
                removeButton.setOnAction(event -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    removeItem(item);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                }
            }
        });
        
        cartTable.getColumns().add(removeCol);
    }

    private void loadCartItems() {
        if (currentUser == null) {
            return;
        }
        
        try {
            List<CartItem> cartItems = cartService.getCartItems();
            cartTable.getItems().setAll(cartItems);
            updateTotal();
        } catch (Exception e) {
            e.printStackTrace();
            ViewManager.showErrorAlert("Hata", "Sepet yüklenirken bir hata oluştu: " + e.getMessage());
        }
    }

    @FXML
    private void updateCart() {
        boolean allUpdated = true;
        try {
            for (CartItem item : cartTable.getItems()) {
                if (!cartService.updateQuantity(item.getId(), item.getQuantity())) {
                    allUpdated = false;
                }
            }
            updateTotal();
            if (allUpdated) {
                showAlert("Başarılı", "Sepet Güncellendi", "Sepetiniz başarıyla güncellendi.");
            } else {
                showAlert("Uyarı", "Bazı ürünler güncellenemedi", "Bazı ürünler stokta yeterli olmayabilir.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ViewManager.showErrorAlert("Hata", "Sepet güncellenirken bir hata oluştu: " + e.getMessage());
        }
    }

    @FXML
    private void handleRemoveAll() {
        if (cartTable.getItems().isEmpty()) {
            showAlert("Uyarı", "Sepet Boş", "Sepetinizde kaldırılacak ürün bulunmamaktadır.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Sepeti Temizle");
        alert.setHeaderText("Sepeti Temizle");
        alert.setContentText("Sepetinizdeki tüm ürünleri kaldırmak istediğinize emin misiniz?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                cartService.clearCart();
                cartTable.getItems().clear();
                updateTotal();
                ViewManager.showInfoAlert("BaÃƒÂ…Ã…Â¸arÃƒÂ„Ã‚Â±lÃƒÂ„Ã‚Â±", "Sepetiniz baÃƒÂ…Ã…Â¸arÃƒÂ„Ã‚Â±yla temizlendi.");
            } catch (Exception e) {
                e.printStackTrace();
                ViewManager.showErrorAlert("Hata", "Sepet temizlenirken bir hata oluştu: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRemoveFromCart() {
        CartItem selectedItem = cartTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            ViewManager.showErrorAlert("Hata", "Lütfen silmek istediğiniz ürünü seçin.");
            return;
        }
        
        try {
            boolean removed = cartService.removeFromCart(selectedItem.getId());
            if (removed) {
                cartTable.getItems().remove(selectedItem);
                updateTotal();
                showAlert("BaÃƒÂ…Ã…Â¸arÃƒÂ„Ã‚Â±lÃƒÂ„Ã‚Â±", "ÃƒÂƒÃ…Â“rÃƒÂƒÃ‚Â¼n KaldÃƒÂ„Ã‚Â±rÃƒÂ„Ã‚Â±ldÃƒÂ„Ã‚Â±", "ÃƒÂƒÃ…Â“rÃƒÂƒÃ‚Â¼n sepetinizden kaldÃƒÂ„Ã‚Â±rÃƒÂ„Ã‚Â±ldÃƒÂ„Ã‚Â±.");
            } else {
                ViewManager.showErrorAlert("Hata", "ÃƒÂƒÃ…Â“rÃƒÂƒÃ‚Â¼n kaldÃƒÂ„Ã‚Â±rÃƒÂ„Ã‚Â±lÃƒÂ„Ã‚Â±rken bir hata oluÃƒÂ…Ã…Â¸tu.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ViewManager.showErrorAlert("Hata", "ÃƒÂƒÃ…Â“rÃƒÂƒÃ‚Â¼n kaldÃƒÂ„Ã‚Â±rÃƒÂ„Ã‚Â±lÃƒÂ„Ã‚Â±rken bir hata oluÃƒÂ…Ã…Â¸tu: " + e.getMessage());
        }
    }

    private void removeItem(CartItem item) {
        try {
            if (cartService.removeFromCart(item.getId())) {
                cartTable.getItems().remove(item);
                updateTotal();
                showAlert("BaÃƒÂ…Ã…Â¸arÃƒÂ„Ã‚Â±lÃƒÂ„Ã‚Â±", "ÃƒÂƒÃ…Â“rÃƒÂƒÃ‚Â¼n KaldÃƒÂ„Ã‚Â±rÃƒÂ„Ã‚Â±ldÃƒÂ„Ã‚Â±", "ÃƒÂƒÃ…Â“rÃƒÂƒÃ‚Â¼n sepetinizden kaldÃƒÂ„Ã‚Â±rÃƒÂ„Ã‚Â±ldÃƒÂ„Ã‚Â±.");
            } else {
                ViewManager.showErrorAlert("Hata", "ÃƒÂƒÃ…Â“rÃƒÂƒÃ‚Â¼n kaldÃƒÂ„Ã‚Â±rÃƒÂ„Ã‚Â±lÃƒÂ„Ã‚Â±rken bir hata oluÃƒÂ…Ã…Â¸tu.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ViewManager.showErrorAlert("Hata", "ÃƒÂƒÃ…Â“rÃƒÂƒÃ‚Â¼n kaldÃƒÂ„Ã‚Â±rÃƒÂ„Ã‚Â±lÃƒÂ„Ã‚Â±rken bir hata oluÃƒÂ…Ã…Â¸tu: " + e.getMessage());
        }
    }

    private void updateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cartTable.getItems()) {
            if (item != null && item.getProduct() != null) {
                BigDecimal itemTotal = item.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
                total = total.add(itemTotal);
            }
        }
        totalLabel.setText(String.format("Toplam: %.2f TL", total));
    }

    @FXML
    private void handleCheckout() {
        if (currentUser == null) {
            showAlert("Hata", "Giriş Gerekli", "Ödeme yapmak için giriş yapmalısınız.");
            return;
        }
        
        if (cartTable.getItems().isEmpty()) {
            showAlert("Uyarı", "Sepet Boş", "Sepetinizde ürün bulunmamaktadır.");
            return;
        }
        
        try {
            cartService.checkout();
            cartTable.getItems().clear();
            updateTotal();
            ViewManager.showInfoAlert("Başarılı", "Siparişiniz başarıyla tamamlandı!");
        } catch (Exception e) {
            e.printStackTrace();
            ViewManager.showErrorAlert("Hata", "Sipariş işlemi sırasında bir hata oluştu: " + e.getMessage());
        }
    }

    @FXML
    private void continueShopping() {
        Stage stage = (Stage) cartContent.getScene().getWindow();
        ViewManager.showHomeView(stage, currentUser);
    }
    
    @FXML
    private void clearCart() {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Sepeti Temizle");
            alert.setHeaderText("Sepeti Temizle");
            alert.setContentText("Sepetinizdeki tüm ürünler silinecek. Emin misiniz?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                cartService.clearCart();
                loadCartItems();
                ViewManager.showInfoAlert("BaÃƒÂ…Ã…Â¸arÃƒÂ„Ã‚Â±lÃƒÂ„Ã‚Â±", "Sepetiniz baÃƒÂ…Ã…Â¸arÃƒÂ„Ã‚Â±yla temizlendi.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ViewManager.showErrorAlert("Hata", "Sepet temizlenirken bir hata oluÃƒÂ…Ã…Â¸tu: " + e.getMessage());
        }
    }
    
    @FXML
    private void checkout() {
        try {
            if (cartService.getCartItems().isEmpty()) {
                ViewManager.showInfoAlert("Sepet Boş", "Sepetinizde ürün bulunmamaktadır.");
                return;
            }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Ödeme İşlemi");
            alert.setHeaderText("Ödeme İşlemini Onayla");
            alert.setContentText("Sepetinizdeki ürünler için ödeme yapmak istediğinize emin misiniz?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                cartService.checkout();
                loadCartItems();
                ViewManager.showInfoAlert("Başarılı", "Ödeme işleminiz başarıyla tamamlandı!");
                Stage stage = (Stage) cartContent.getScene().getWindow();
                ViewManager.showHomeView(stage, currentUser);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ViewManager.showErrorAlert("Hata", "Ödeme işlemi sırasında bir hata oluştu: " + e.getMessage());
        }
    }
}
