package source.eticaret.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.*;
import source.eticaret.model.Category;
import source.eticaret.model.Product;
import source.eticaret.model.User;
import source.eticaret.service.*;
import source.eticaret.view.ViewManager;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class HomeController {
    @FXML private TextField searchField;
    @FXML private VBox categoriesContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private GridPane productsGrid;
    @FXML private GridPane categoryProductsGrid;
    @FXML private VBox featuredProductsContainer;
    @FXML private VBox categoryProductsContainer;
    @FXML private Label categoryTitle;
    @FXML private Label sectionTitle;
    @FXML private Label welcomeLabel;
    @FXML private Label cartBadge;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Button logoutButton;
    @FXML private Button accountButton;
    @FXML private Button cartButton;
    @FXML private Button profileButton;
    @FXML private Button sellButton;

    private final ProductService productService;
    private final CategoryService categoryService;
    private final AuthService authService;
    private final CartService cartService;
    private User currentUser;
    private Long currentCategoryId = null;
    private String currentSearchQuery = "";
    

    private static final int ITEMS_PER_PAGE = 15;
    private int currentPage = 0;
    private boolean hasMoreProducts = true;
    private boolean categoriesLoaded = false;

    public HomeController() {
        DatabaseService dbService = DatabaseService.getInstance();
        this.productService = new ProductService(dbService);
        this.categoryService = new CategoryService(dbService);
        this.authService = AuthService.getInstance(dbService);
        this.cartService = new CartService(dbService);
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateWelcomeMessage();
        updateUIForUser();
    }
    
    private void updateWelcomeMessage() {
        if (welcomeLabel != null) {
            if (currentUser != null) {
                welcomeLabel.setText("Hoş geldiniz, " + currentUser.getUsername() + "!");
                if (profileButton != null) profileButton.setVisible(true);
                if (sellButton != null) sellButton.setVisible(true);
                if (cartButton != null) cartButton.setVisible(true);
                updateCartBadge();
                
                if (sellButton != null) {
                    sellButton.setDisable(!currentUser.isSeller());
                }
            } else {
                welcomeLabel.setText("Hoş geldiniz, lütfen giriş yapın!");
                if (profileButton != null) profileButton.setVisible(false);
                if (sellButton != null) sellButton.setVisible(false);
                if (cartButton != null) cartButton.setVisible(false);
            }
        }
    }
    
    private void updateUIForUser() {
        if (loginButton != null) {
            loginButton.setVisible(currentUser == null);
        }
        if (registerButton != null) {
            registerButton.setVisible(currentUser == null);
        }
        if (logoutButton != null) {
            logoutButton.setVisible(currentUser != null);
        }
        if (accountButton != null) {
            accountButton.setVisible(currentUser != null);
        }
        if (cartButton != null) {
            cartButton.setVisible(currentUser != null);
        }
        if (currentUser != null) {
            updateCartBadge();
        } else if (cartBadge != null) {
            cartBadge.setVisible(false);
        }
        loadProducts();
    }
    
    private void updateCartBadge() {
        if (currentUser != null) {
            int itemCount = cartService.getCartItemCount();
            cartButton.setText(itemCount > 0 ? String.format("Sepetim (%d)", itemCount) : "Sepetim");
        }
    }

    @FXML
    public void openProfile() {
        if (currentUser != null) {
            Stage stage = (Stage) profileButton.getScene().getWindow();
            ViewManager.showAccountView(stage, currentUser);
        } else {
            ViewManager.showErrorAlert("Giriş Gerekli", "Profil görüntülemek için giriş yapmalısınız.");
        }
    }

    @FXML
    private void initialize() {
        try {
        
            if (currentUser == null && authService.isUserLoggedIn()) {
                currentUser = authService.getCurrentUser();
                updateUIForUser();
            }
            

            if (featuredProductsContainer != null) {
                featuredProductsContainer.setVisible(true);
                featuredProductsContainer.setManaged(true);
            }
            if (categoryProductsContainer != null) {
                categoryProductsContainer.setVisible(false);
                categoryProductsContainer.setManaged(false);
            }
            

            loadCategories();
            loadFeaturedProducts();
            updateCartBadge();
            cartButton.setOnAction(e -> {
                if (currentUser != null) {
                    ViewManager.showCartView((Stage) cartButton.getScene().getWindow(), currentUser);
                } else {
                    ViewManager.showErrorAlert("Giriş Gerekli", "Sepetinizi görüntülemek için giriş yapmalısınız.");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();

            if (sectionTitle != null) {
                sectionTitle.setText("Bir hata oluÃƒÂ…Ã…Â¸tu");
            }
        }
    }

    private void loadCategories() {
        if (categoriesContainer == null) {
            System.err.println("categoriesContainer is null");
            return;
        }
        
        try {

            Platform.runLater(() -> {
                try {
                    categoriesContainer.getChildren().clear();
                    

                    Button featuredButton = createCategoryButton("Öne Çıkan Ürünler", -1L);
                    featuredButton.setOnAction(e -> showFeaturedProducts());
                    

                    Button allProductsButton = createCategoryButton("Tüm Ürünler", 0L);
                    allProductsButton.setOnAction(e -> showAllProducts());
                    

                    categoriesContainer.getChildren().addAll(featuredButton, allProductsButton);
                    

                    List<Category> mainCategories = categoryService.getRootCategories();
                    

                    mainCategories.forEach(category -> {
                        if (category != null && category.getName() != null) {
                            Button categoryButton = createCategoryButton(
                                category.getName(), 
                                category.getId()
                            );
                            categoryButton.setOnAction(e -> filterByCategory(category.getId()));
                            categoriesContainer.getChildren().add(categoryButton);
                        }
                    });
                    
                    categoriesContainer.setFillWidth(true);
                    categoriesLoaded = true;
                    

                    showFeaturedProducts();
                    
                } catch (Exception e) {
                    System.err.println("Error loading categories: ");
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.err.println("Error in loadCategories: ");
            e.printStackTrace();
        }
    }

    private Button createCategoryButton(String name, Long categoryId) {
        Button button = new Button(name);
        button.getStyleClass().add("category-button");
        button.setUserData(categoryId);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setPadding(new Insets(10, 15, 10, 15));
        button.setContentDisplay(ContentDisplay.LEFT);
        button.setStyle("-fx-font-size: 14px; -fx-text-fill: #333; -fx-background-radius: 4;");
        
        button.setOnMouseEntered(e -> {
            if (!button.getStyleClass().contains("active-category")) {
                button.setStyle("-fx-background-color: #f0f0f0; -fx-font-size: 14px; -fx-text-fill: #333; -fx-background-radius: 4;");
            }
        });
        
        button.setOnMouseExited(e -> {
            if (!button.getStyleClass().contains("active-category")) {
                button.setStyle("-fx-background-color: transparent; -fx-font-size: 14px; -fx-text-fill: #333; -fx-background-radius: 4;");
            }
        });
        

        if ("ÃƒÂƒÃ¢Â€Â“ne ÃƒÂƒÃ¢Â€Â¡ÃƒÂ„Ã‚Â±kan ÃƒÂƒÃ…Â“rÃƒÂƒÃ‚Â¼nler".equals(name)) {
            button.setOnAction(e -> showFeaturedProducts());
        } else {
            button.setOnAction(e -> filterByCategory(categoryId));
        }
        
        return button;
    }

    private boolean isFiltering = false;


    private void showFeaturedProducts() {
        if (isFiltering) return;

        try {
            isFiltering = true;
            currentCategoryId = -1L;
            currentSearchQuery = "";

            if (searchField != null) {
                searchField.clear();
            }
            if (featuredProductsContainer != null) {
                featuredProductsContainer.setVisible(true);
                featuredProductsContainer.setManaged(true);
            }
            if (categoryProductsContainer != null) {
                categoryProductsContainer.setVisible(false);
                categoryProductsContainer.setManaged(false);
            }
            if (sectionTitle != null) {
                sectionTitle.setText("ÃƒÂƒÃ¢Â€Â“ne ÃƒÂƒÃ¢Â€Â¡ÃƒÂ„Ã‚Â±kan ÃƒÂƒÃ…Â“rÃƒÂƒÃ‚Â¼nler");
            }


            loadFeaturedProducts();
            if (categoriesContainer != null) {
                Platform.runLater(() -> {
                    for (Node node : categoriesContainer.getChildren()) {
                        if (node instanceof Button button) {
                            button.getStyleClass().remove("active-category");
                            button.setStyle("-fx-background-color: transparent; -fx-font-size: 14px; -fx-text-fill: #333; -fx-background-radius: 4;");


                            if (button.getUserData() != null && button.getUserData().equals(-1L)) {
                                button.getStyleClass().add("active-category");
                                button.setStyle("-fx-background-color: #e0e0e0; -fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1976D2; -fx-background-radius: 4;");
                            }
                        }
                    }
                });
            }
        } finally {
            isFiltering = false;
        }
    }
    
    private void filterByCategory(Long categoryId) {
        if (isFiltering || categoriesContainer == null) return;
        
        try {
            isFiltering = true;
            

            currentSearchQuery = "";
            if (searchField != null) {
                searchField.clear();
            }
            

            Platform.runLater(() -> {
                for (Node node : categoriesContainer.getChildren()) {
                    if (node instanceof Button button) {
                        button.getStyleClass().remove("active-category");
                        button.setStyle("-fx-background-color: transparent; -fx-font-size: 14px; -fx-text-fill: #333; -fx-background-radius: 4;");
                        

                        Object buttonData = button.getUserData();
                        if ((buttonData != null && buttonData.equals(categoryId)) || 
                            (categoryId == 0L && "Tüm Ürünler".equals(button.getText())) ||
                            (categoryId == -1L && "Öne Çıkan Ürünler".equals(button.getText()))) {
                            
                            button.getStyleClass().add("active-category");
                            button.setStyle("-fx-background-color: #e0e0e0; -fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1976D2; -fx-background-radius: 4;");
                        }
                    }
                }
            });
            

            currentCategoryId = categoryId;
            
            if (categoryId == null || categoryId == -1L) {

                showFeaturedProducts();
            } else if (categoryId == 0L) {

                showAllProducts();
            } else {

                showCategoryProducts(categoryId);
            }
        } finally {
            isFiltering = false;
        }
    }
    
    private void loadFeaturedProducts() {
        Platform.runLater(() -> {
            try {

                if (featuredProductsContainer != null) {
                    featuredProductsContainer.setVisible(true);
                    featuredProductsContainer.setManaged(true);
                }
                

                if (categoryProductsContainer != null) {
                    categoryProductsContainer.setVisible(false);
                    categoryProductsContainer.setManaged(false);
                }
                
                if (sectionTitle != null) {
                    sectionTitle.setText("Öne Çıkan Ürünler");
                }
                

                if (productsGrid != null) {
                    productsGrid.getChildren().clear();
                    productsGrid.getColumnConstraints().clear();
                    productsGrid.getRowConstraints().clear();
                    

                    for (int i = 0; i < 5; i++) {
                        ColumnConstraints colConst = new ColumnConstraints();
                        colConst.setHgrow(Priority.ALWAYS);
                        colConst.setFillWidth(true);
                        colConst.setPercentWidth(20);
                        colConst.setMaxWidth(Double.MAX_VALUE);
                        productsGrid.getColumnConstraints().add(colConst);
                    }
                    

                    List<Product> allProducts = productService.getAllProducts();
                    if (allProducts != null && !allProducts.isEmpty()) {

                        Collections.shuffle(allProducts);
                        

                        int col = 0;
                        int row = 0;
                        int maxProducts = Math.min(4, allProducts.size());
                        
                        for (int i = 0; i < maxProducts; i++) {
                            VBox productCard = createProductCard(allProducts.get(i));
                            if (productCard != null) {
                                GridPane.setMargin(productCard, new Insets(10));
                                productsGrid.add(productCard, col, row);
                                
                                col++;
                                if (col >= 4) {
                                    col = 0;
                                    row++;
                                }
                            }
                        }
                        

                        for (int i = 0; i <= row; i++) {
                            RowConstraints rowConst = new RowConstraints();
                            rowConst.setVgrow(Priority.ALWAYS);
                            rowConst.setFillHeight(true);
                            productsGrid.getRowConstraints().add(rowConst);
                        }
                    } else {

                        Label noProductsLabel = new Label("Henüz hiç ürün bulunmamaktadır.");
                        noProductsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
                        productsGrid.add(noProductsLabel, 0, 0, 4, 1);
                    }
                    
                    productsGrid.requestLayout();
                }
                
                if (featuredProductsContainer != null) {
                    featuredProductsContainer.requestLayout();
                }
                
            } catch (Exception e) {
                System.err.println("Error loading featured products: ");
                e.printStackTrace();
                

                if (productsGrid != null) {
                    Label errorLabel = new Label("Ürünler yüklenirken bir hata oluştu.");
                    errorLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 14px;");
                    productsGrid.add(errorLabel, 0, 0);
                }
            }
        });
    }
    
    private void loadProducts() {

        currentPage = 0;
        hasMoreProducts = true;
        

        if (productsGrid != null) {
            productsGrid.getChildren().clear();
            productsGrid.getColumnConstraints().clear();
            productsGrid.getRowConstraints().clear();
            

            for (int i = 0; i < 4; i++) {
                ColumnConstraints colConst = new ColumnConstraints();
                colConst.setHgrow(Priority.ALWAYS);
                colConst.setFillWidth(true);
                colConst.setPercentWidth(25);
                colConst.setMaxWidth(Double.MAX_VALUE);
                productsGrid.getColumnConstraints().add(colConst);
            }
            

            loadMoreProducts();
        }
    }

    private void showCategoryProducts(Long categoryId) {
        Platform.runLater(() -> {
            try {
                if (categoryProductsContainer == null) return;
                

                categoryProductsContainer.setVisible(true);
                categoryProductsContainer.setManaged(true);
                if (featuredProductsContainer != null) {
                    featuredProductsContainer.setVisible(false);
                    featuredProductsContainer.setManaged(false);
                }
                
                String title;
                List<Product> products;
                
                if (categoryId == null) {

                    title = "Tüm Ürünler";
                    products = productService.getAllProducts();
                } else {

                    title = categoryService.getCategoryById(categoryId)
                            .map(cat -> cat.getName() + " Ürünleri")
                            .orElse("Kategori Ürünleri");
                    products = productService.getProductsByCategory(categoryId, 0, 100);
                }
                
                categoryTitle.setText(title);
                

                categoryProductsGrid.getChildren().clear();
                

                categoryProductsGrid.getColumnConstraints().clear();
                categoryProductsGrid.getRowConstraints().clear();
                

                for (int i = 0; i < 4; i++) {
                    ColumnConstraints colConst = new ColumnConstraints();
                    colConst.setHgrow(Priority.ALWAYS);
                    colConst.setFillWidth(true);
                    colConst.setPercentWidth(25);
                    categoryProductsGrid.getColumnConstraints().add(colConst);
                }
                

                int col = 0;
                int row = 0;
                for (Product product : products) {
                    VBox productCard = createProductCard(product);
                    

                    GridPane.setMargin(productCard, new Insets(10));
                    

                    categoryProductsGrid.add(productCard, col, row);
                    
                    col++;
                    if (col >= 4) {
                        col = 0;
                        row++;
                    }
                }
                

                for (int i = 0; i <= row; i++) {
                    RowConstraints rowConst = new RowConstraints();
                    rowConst.setVgrow(Priority.ALWAYS);
                    rowConst.setFillHeight(true);
                    categoryProductsGrid.getRowConstraints().add(rowConst);
                }
                

                categoryProductsContainer.requestLayout();
                categoryProductsGrid.requestLayout();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void showAllProducts() {
        if (isFiltering) return;

        try {
            isFiltering = true;
            currentCategoryId = 0L;
            currentSearchQuery = "";

            if (searchField != null) {
                searchField.clear();
            }
            if (featuredProductsContainer != null) {
                featuredProductsContainer.setVisible(true);
                featuredProductsContainer.setManaged(true);
            }
            if (categoryProductsContainer != null) {
                categoryProductsContainer.setVisible(false);
                categoryProductsContainer.setManaged(false);
            }
            if (sectionTitle != null) {
                sectionTitle.setText("Tüm Ürünler");
                sectionTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");
            }


            currentPage = 0;
            hasMoreProducts = true;
            if (productsGrid != null) {
                productsGrid.getChildren().clear();
                loadMoreProducts();
            }
            if (categoriesContainer != null) {
                Platform.runLater(() -> {
                    for (Node node : categoriesContainer.getChildren()) {
                        if (node instanceof Button button) {
                            button.getStyleClass().remove("active-category");
                            button.setStyle("-fx-background-color: transparent; -fx-font-size: 14px; -fx-text-fill: #333; -fx-background-radius: 4;");


                            if (button.getUserData() != null && button.getUserData().equals(0L)) {
                                button.getStyleClass().add("active-category");
                                button.setStyle("-fx-background-color: #e0e0e0; -fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1976D2; -fx-background-radius: 4;");
                            }
                        }
                    }
                });
            }
        } finally {
            isFiltering = false;
        }
    }


    private void loadMoreProducts() {
        if (!hasMoreProducts) return;
        
        List<Product> products;
        int offset = currentPage * ITEMS_PER_PAGE;
        
        try {

            if (currentCategoryId != null && currentCategoryId > 0) {

                products = productService.getProductsByCategory(currentCategoryId, offset, ITEMS_PER_PAGE + 1);
            } else if (!currentSearchQuery.isEmpty()) {

                products = productService.searchProducts(currentSearchQuery, offset, ITEMS_PER_PAGE + 1);
            } else {

                products = productService.getProducts(offset, ITEMS_PER_PAGE + 1);
            }
            

            if (products.size() > ITEMS_PER_PAGE) {
                hasMoreProducts = true;

                products = products.subList(0, ITEMS_PER_PAGE);
            } else {
                hasMoreProducts = false;
            }
            

            if (currentPage == 0) {
                productsGrid.getChildren().clear();
                

                productsGrid.getColumnConstraints().clear();
                for (int i = 0; i < 5; i++) {
                    ColumnConstraints colConst = new ColumnConstraints();
                    colConst.setHgrow(Priority.ALWAYS);
                    colConst.setFillWidth(true);
                    colConst.setPercentWidth(20);
                    colConst.setMaxWidth(Double.MAX_VALUE);
                    productsGrid.getColumnConstraints().add(colConst);
                }
            }
            

            int columns = 5;
            int itemsPerPage = ITEMS_PER_PAGE;
            int row = (currentPage * itemsPerPage) / columns;
            int col = (currentPage * itemsPerPage) % columns;
            

            for (Product product : products) {
                VBox productCard = createProductCard(product);
                

                int finalRow = row;
                int finalCol = col;
                productsGrid.getChildren().removeIf(node ->
                    GridPane.getRowIndex(node) == finalRow && GridPane.getColumnIndex(node) == finalCol);
                    
                productsGrid.add(productCard, col, row);
                

                col++;
                if (col >= columns) {
                    col = 0;
                    row++;
                }
            }
            
            currentPage++;
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Hata", "Ürünler yüklenirken bir hata oluştu: " + e.getMessage());
        }
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);
        try {
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Image image = new Image(product.getImageUrl(), 180, 180, true, true, true);
                imageView.setImage(image);
            } else {
                imageView.setImage(createPlaceholderImage(180, 180, "Resim Yok"));
            }
        } catch (Exception e) {
            imageView.setImage(createPlaceholderImage(180, 180, "Resim Yüklenemedi"));
        }
        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333; -fx-wrap-text: true;");
        nameLabel.setMaxWidth(180);
        nameLabel.setWrapText(true);
        Label priceLabel = new Label(formatPrice(product.getPrice()));
        priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e67e22;");
        Button addToCartBtn = new Button("Sepete Ekle");
        addToCartBtn.setStyle(
            "-fx-background-color: #4CAF50; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 8 16; " +
            "-fx-background-radius: 4;"
        );
        addToCartBtn.setOnAction(e -> addToCart(product));

        card.getChildren().addAll(imageView, nameLabel, priceLabel, addToCartBtn);
        card.setUserData(product);
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: #f9f9f9; " +
            "-fx-background-radius: 5; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 5; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);"
        ));
        card.setOnMouseClicked(e -> showProductDetails(product));
        
        return card;
    }
    
    private Image createPlaceholderImage(int width, int height, String text) {
        WritableImage img = new WritableImage(width, height);
        PixelWriter pw = img.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pw.setColor(x, y, Color.LIGHTGRAY);
            }
        }
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(width, height);
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.setFont(javafx.scene.text.Font.font(12));
        javafx.scene.text.Text textNode = new javafx.scene.text.Text(text);
        textNode.setFont(gc.getFont());
        double textWidth = textNode.getLayoutBounds().getWidth();
        double x = (width - textWidth) / 2;
        double y = height / 2;
        
        gc.fillText(text, x, y);
        javafx.embed.swing.SwingFXUtils.toFXImage(
            javafx.embed.swing.SwingFXUtils.fromFXImage(canvas.snapshot(null, null), null), 
            img
        );
        return img;
    }
    
    private Image createColorPlaceholder(int width, int height, Color color) {
        WritableImage img = new WritableImage(width, height);
        PixelWriter pw = img.getPixelWriter();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pw.setColor(x, y, color);
            }
        }
        return img;
    }
    
    private String formatPrice(BigDecimal price) {
        return NumberFormat.getCurrencyInstance(new Locale("tr", "TR")).format(price);
    }

    private void addToCart(Product product) {
        if (product == null) return;
        
        if (currentUser == null) {
            ViewManager.showAlert("Giriş Gerekli", "Sepete eklemek için giriş yapmalısınız.", "Lütfen giriş yapın veya kayıt olun.");
            ViewManager.showLoginView((Stage) productsGrid.getScene().getWindow());
            return;
        }
        
        try {
            boolean success = cartService.addToCart(product.getId(), 1);
            if (success) {
                updateCartBadge();
                ViewManager.showAlert("Başarılı", "Ürün Sepete Eklendi", 
                    String.format("%s sepete eklendi.\nSepetinizde toplam %d ürün bulunmaktadır.", 
                    product.getName(), cartService.getCartItemCount()));
            } else {
                ViewManager.showErrorAlert("Hata", "Ürün sepete eklenemedi. Stokta yeteri ürün olmayabilir.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ViewManager.showErrorAlert("Hata", "Ürün sepete eklenirken bir hata oluştu: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (!query.equals(currentSearchQuery)) {
            currentSearchQuery = query;
            currentCategoryId = null;
            loadProducts();
        }
    }

    @FXML
    private void openCart() {
        if (currentUser != null) {
            ViewManager.showCartView((Stage) cartButton.getScene().getWindow(), currentUser);
        } else {
            ViewManager.showErrorAlert("Giriş Gerekli", "Sepetinizi görüntülemek için giriş yapmalısınız.");
        }
    }

    @FXML
    private void startSelling() {
        if (currentUser != null && currentUser.isSeller()) {
            ViewManager.showSellerDashboard((Stage) sellButton.getScene().getWindow());
        } else {
            ViewManager.showErrorAlert("Yetki Gerekli", "Bu işlem için satıcı hesabına ihtiyacınız var.");
        }
    }

    private void setupScrollListener() {

        Platform.runLater(() -> {
            try {

                ScrollPane scrollPane = findScrollPane(productsGrid);
                if (scrollPane != null) {
                    scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue.doubleValue() >= 0.95) {

                            loadMoreProducts();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    private ScrollPane findScrollPane(Node node) {
        if (node == null) {
            return null;
        }
        
        if (node instanceof ScrollPane) {
            return (ScrollPane) node;
        }
        
        if (node.getParent() != null) {
            return findScrollPane(node.getParent());
        }
        
        return null;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showProductDetails(Product product) {
        if (product == null) return;
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ürün Detayları");
        alert.setHeaderText(product.getName());
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        ImageView imageView = new ImageView();
        try {
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Image image = new Image(product.getImageUrl(), 200, 200, true, true);
                imageView.setImage(image);
            } else {
                imageView.setImage(createPlaceholderImage(200, 200, "Resim Yok"));
            }
        } catch (Exception e) {
            imageView.setImage(createPlaceholderImage(200, 200, "Resim Yüklenemedi"));
        }
        imageView.setFitWidth(200);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);
        Label priceLabel = new Label("Fiyat: " + formatPrice(product.getPrice()));
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #e67e22;");
        
        Label stockLabel = new Label("Stok Durumu: " + 
            (product.getStockQuantity() > 0 ? "Stokta Var" : "Stokta Yok"));
        stockLabel.setStyle("-fx-font-size: 14px;");
        
        Label descriptionLabel = new Label("Açıklama: " + 
            (product.getDescription() != null ? product.getDescription() : "Açıklama bulunmuyor."));
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(350);
        Button addToCartBtn = new Button("Sepete Ekle");
        addToCartBtn.setStyle(
            "-fx-background-color: #4CAF50; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 8 24; " +
            "-fx-background-radius: 4;"
        );
        addToCartBtn.setOnAction(e -> {
            addToCart(product);
            alert.close();
        });
        
        content.getChildren().addAll(imageView, priceLabel, stockLabel, descriptionLabel, addToCartBtn);
        content.setAlignment(Pos.CENTER);
        
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }
}
