package source.eticaret.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import source.eticaret.Main;
import source.eticaret.model.User;
import source.eticaret.service.AuthService;
import source.eticaret.view.ViewManager;

public class AuthController {
    @FXML private TextField loginUsernameField;
    @FXML private PasswordField loginPasswordField;
    @FXML private Label loginErrorLabel;
    @FXML private TextField registerUsernameField;
    @FXML private PasswordField registerPasswordField;
    @FXML private TextField registerEmailField;
    @FXML private Label registerErrorLabel;

    @FXML
    private void handleLogin() {
        String username = loginUsernameField.getText().trim();
        String password = loginPasswordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            ViewManager.showErrorAlert("Hata", "Lütfen kullanıcı adı ve şifre giriniz.");
            return;
        }

        AuthService authService = AuthService.getInstance(Main.getDatabaseService());
        if (authService.login(username, password)) {
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                Stage stage = (Stage) loginUsernameField.getScene().getWindow();
                if (currentUser.isSeller()) {
                    ViewManager.showSellerDashboard(stage);
                } else {
                    ViewManager.showHomeView(stage, currentUser);
                }
            }
        } else {
            ViewManager.showErrorAlert("Hata", "Geçersiz kullanıcı adı veya şifre!");
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    @FXML
    private void handleRegister() {
        String username = registerUsernameField.getText().trim();
        String email = registerEmailField.getText().trim();
        String password = registerPasswordField.getText().trim();
        String roleName = "ROLE_CUSTOMER";

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            ViewManager.showErrorAlert("Hata", "Lütfen tüm alanları doldurun.");
            return;
        }
        
        if (!isValidEmail(email)) {
            ViewManager.showErrorAlert("Hata", "Lütfen geçerli bir e-posta adresi giriniz.");
            return;
        }

        AuthService authService = AuthService.getInstance(Main.getDatabaseService());
        if (authService.register(username, email, password, roleName)) {
            ViewManager.showErrorAlert("Başarılı", "Kayıt başarılı! Giriş yapabilirsiniz.");
            ViewManager.showLoginView((Stage) registerUsernameField.getScene().getWindow());
        } else {
            ViewManager.showErrorAlert("Hata", "Bu kullanıcı adı zaten kullanılıyor.");
        }
    }

    @FXML
    private void switchToLogin() {
        ViewManager.showLoginView((Stage) registerUsernameField.getScene().getWindow());
    }

    @FXML
    private void switchToRegister() {
        ViewManager.showRegisterView((Stage) loginUsernameField.getScene().getWindow());
    }

    public static void showLoginView(Stage stage) {
        ViewManager.showLoginView(stage);
    }

    public static void redirectToLogin(Stage stage) {
        showLoginView(stage);
    }

    public static void showHomeView(Stage stage) {
        AuthService authService = AuthService.getInstance(Main.getDatabaseService());
        if (!authService.isUserLoggedIn()) {
            redirectToLogin(stage);
            return;
        }

        ViewManager.showHomeView(stage, authService.getCurrentUser());
    }

    public static void showSellerDashboard(Stage stage) {
        ViewManager.showSellerDashboard(stage);
    }

    public static void showErrorAlert(String title, String message) {
        ViewManager.showErrorAlert(title, message);
    }
}