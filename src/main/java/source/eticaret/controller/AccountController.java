package source.eticaret.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import source.eticaret.model.User;
import source.eticaret.view.ViewManager;

public class AccountController {
    @FXML private Label userName;
    @FXML private Label userEmail;
    @FXML private Label userRole;
    @FXML private ImageView userAvatar;
    @FXML private VBox accountContent;

    private User currentUser;

    @FXML
    public void initialize() {
    }

    public void setUser(User user) {
        this.currentUser = user;
        updateUserInfo();
    }

    private void updateUserInfo() {
        if (currentUser != null) {
            userName.setText(currentUser.getUsername());
            userEmail.setText(currentUser.getEmail());
            String role = currentUser.isSeller() ? "Satıcı" : "Müşteri";
            userRole.setText(role);
        }
    }

    @FXML
    private void goBack() {
        Stage stage = (Stage) accountContent.getScene().getWindow();
        ViewManager.showHomeView(stage, currentUser);
    }

    @FXML
    private void showProfile() {
        Stage stage = (Stage) accountContent.getScene().getWindow();
        ViewManager.showProfileEditView(stage, currentUser);
    }

    @FXML
    private void showAddresses() {
        Stage stage = (Stage) accountContent.getScene().getWindow();
        ViewManager.showAddressListView(stage, currentUser);
    }

    @FXML
    private void showOrders() {
        Stage stage = (Stage) accountContent.getScene().getWindow();
        ViewManager.showAlert("Sipariş Geçmişi", "Siparişleriniz", "Sipariş geçmişiniz burada listelenecek.");
    }

    @FXML
    private void showFavorites() {
        Stage stage = (Stage) accountContent.getScene().getWindow();
        ViewManager.showAlert("Favorilerim", "Favori Ürünlerim", "Favori ürünleriniz burada listelenecek.");
    }

    @FXML
    private void logout() {
        Stage stage = (Stage) accountContent.getScene().getWindow();
        currentUser = null;
        ViewManager.showLoginView(stage);
    }
}
