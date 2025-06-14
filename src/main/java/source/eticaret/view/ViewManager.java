package source.eticaret.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.application.Platform;
import source.eticaret.Main;
import source.eticaret.controller.*;
import source.eticaret.model.*;
import source.eticaret.service.AuthService;

import java.io.IOException;
import java.util.Optional;

import static source.eticaret.controller.AuthController.showErrorAlert;

public class ViewManager {
    private static Stage primaryStage;
    
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }
    
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    private static final String HOME_VIEW = "/templates/home/index.fxml";
    private static final String LOGIN_VIEW = "/templates/auth/login.fxml";
    private static final String SELLER_DASHBOARD_VIEW = "/templates/seller/dashboard.fxml";
    private static final String ACCOUNT_VIEW = "/templates/account/account.fxml";
    private static final String PROFILE_EDIT_VIEW = "/templates/account/profile_edit.fxml";
    private static final String ADDRESS_LIST_VIEW = "/templates/account/address_list.fxml";
    private static final String REGISTER_VIEW = "/templates/auth/register.fxml";
    private static final String CART_VIEW = "/templates/cart/cart.fxml";

    public static void showHomeView(Stage stage, User user) {
        try {
            if (stage == null) {
                stage = primaryStage;
                if (stage == null) {
                    stage = new Stage();
                    setPrimaryStage(stage);
                }
            }
            
            FXMLLoader loader = new FXMLLoader(ViewManager.class.getResource(HOME_VIEW));
            Parent root = loader.load();

            HomeController homeController = loader.getController();
            if (homeController != null) {
                homeController.setCurrentUser(user);
                
                Scene scene = new Scene(root, 1200, 800);
                stage.setScene(scene);
                stage.setTitle("E-Ticaret Uygulaması");
                stage.setMinWidth(1000);
                stage.setMinHeight(700);
                stage.setMaximized(true);
                stage.show();
            } else {
                throw new IllegalStateException("HomeController could not be initialized");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Hata", "Ana sayfa yüklenirken bir hata oluştu: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Hata", "Beklenmeyen bir hata oluştu: " + e.getMessage());
        }
    }

    public static void showAccountView(Stage stage, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewManager.class.getResource(ACCOUNT_VIEW));
            Parent root = loader.load();

            AccountController controller = loader.getController();
            controller.setUser(user);

            Scene scene = new Scene(root, 1200, 800);
            stage.setScene(scene);
            stage.setTitle("Hesabım - E-Ticaret Uygulaması");
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Hata", "Hesap sayfası yüklenirken bir hata oluştu.");
        }
    }

    public static void showErrorAlert(String title, String message) {
        showAlert(title, title, message, Alert.AlertType.ERROR);
    }
    
    public static void showAlert(String title, String header, String content) {
        showAlert(title, header, content, Alert.AlertType.INFORMATION);
    }
    
    public static void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    public static void showProfileEditView(Stage stage, User user) {
        try {
            if (stage == null) {
                stage = primaryStage;
                if (stage == null) {
                    stage = new Stage();
                    setPrimaryStage(stage);
                }
            }
            
            FXMLLoader loader = new FXMLLoader(ViewManager.class.getResource(PROFILE_EDIT_VIEW));
            Parent root = loader.load();

            ProfileEditController controller = loader.getController();
            if (controller != null) {
                controller.setUser(user);
                
                Scene scene = new Scene(root, 800, 600);
                stage.setScene(scene);
                stage.setTitle("Profili Düzenle - E-Ticaret Uygulaması");
                stage.setMinWidth(600);
                stage.setMinHeight(500);
                stage.show();
            } else {
                showErrorAlert("Hata", "Profil düzenleme sayfası yüklenirken bir hata oluştu.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Hata", "Profil düzenleme sayfası yüklenirken bir hata oluştu: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Hata", "Beklenmeyen bir hata oluÃƒÂ…Ã…Â¸tu: " + e.getMessage());
        }
    }
    
    public static void showAddressListView(Stage stage, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewManager.class.getResource(ADDRESS_LIST_VIEW));
            Parent root = loader.load();

            AddressListController controller = loader.getController();
            controller.setUser(user);

            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);
            stage.setTitle("Adreslerim - E-Ticaret Uygulaması");
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Hata", "Adresler sayfası yüklenirken bir hata oluştu.");
        }
    }
    


    public static void showSellerDashboard(Stage stage) {
        try {
            AuthService authService = AuthService.getInstance(Main.getDatabaseService());
            if (!authService.isUserLoggedIn()) {
                showErrorAlert("Hata", "Satıcı paneline erişmek için giriş yapmalısınız.");
                showLoginView(stage);
                return;
            }
            
            User currentUser = authService.getCurrentUser();
            if (currentUser == null || !currentUser.isSeller()) {
                showErrorAlert("Yetki Hatası", "Bu sayfaya erişim yetkiniz yok.");
                showHomeView(stage, currentUser);
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(ViewManager.class.getResource(SELLER_DASHBOARD_VIEW));
            Parent root = loader.load();
            SellerDashboardController controller = loader.getController();
            controller.setUser(currentUser);

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Satıcı Paneli - E-Ticaret Uygulaması");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Hata", "Satıcı paneli yüklenirken bir hata oluştu: " + e.getMessage());
        }
    }

    public static void showRegisterView(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewManager.class.getResource(REGISTER_VIEW));
            Parent root = loader.load();

            Scene scene = new Scene(root, 400, 600);
            if (stage == null) {
                stage = primaryStage;
                if (stage == null) {
                    stage = new Stage();
                    setPrimaryStage(stage);
                }
            }
            stage.setScene(scene);
            stage.setTitle("Kayıt Ol - E-Ticaret Uygulaması");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Hata", "Kayıt sayfası yüklenirken bir hata oluştu.");
        }
    }
    
    public static void showLoginView(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewManager.class.getResource(LOGIN_VIEW));
            Parent root = loader.load();

            Scene scene = new Scene(root, 400, 500);
            if (stage == null) {
                stage = primaryStage;
                if (stage == null) {
                    stage = new Stage();
                    setPrimaryStage(stage);
                }
            }
            stage.setScene(scene);
            stage.setTitle("Giriş Yap - E-Ticaret Uygulaması");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Hata", "Giriş sayfası yüklenirken bir hata oluştu.");
        }
    }
    
    public static void showCartView(Stage stage, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewManager.class.getResource(CART_VIEW));
            Parent root = loader.load();

            CartController controller = loader.getController();
            controller.setUser(user);

            Scene scene = new Scene(root, 1200, 800);
            if (stage == null) {
                stage = primaryStage;
                if (stage == null) {
                    stage = new Stage();
                    setPrimaryStage(stage);
                }
            }
            stage.setScene(scene);
            stage.setTitle("Sepetim - E-Ticaret Uygulaması");
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Hata", "Sepet sayfası yüklenirken bir hata oluştu.");
        }
    }
}