package source.eticaret.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import source.eticaret.model.User;
import source.eticaret.view.ViewManager;

public class ProfileEditController {
    @FXML
    private VBox root;
    private AccountController accountController;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    
    private User currentUser;
    
    public void setUser(User user) {
        this.currentUser = user;
        loadUserData();
    }
    
    private void loadUserData() {
        if (currentUser != null) {
            fullNameField.setText(currentUser.getUsername());
            emailField.setText(currentUser.getEmail());
        }
    }
    
    @FXML
    private void saveProfile() {
        try {
            currentUser.setUsername(fullNameField.getText().trim());
            currentUser.setEmail(emailField.getText().trim());
            ViewManager.showAlert("Başarılı", "Başarılı", "Profil bilgileriniz güncellendi.");
            cancel();
        } catch (Exception e) {
            e.printStackTrace();
            ViewManager.showErrorAlert("Hata", "Profil güncellenirken bir hata oluştu: " + e.getMessage());
        }
    }
    
    @FXML
    private void cancel() {
        try {
            Node source = fullNameField != null ? fullNameField : 
                          emailField != null ? emailField : 
                          phoneField;
            
            if (source != null && source.getScene() != null) {
                Stage stage = (Stage) source.getScene().getWindow();
                ViewManager.showAccountView(stage, currentUser);
            } else if (root != null && root.getScene() != null) {
                Stage stage = (Stage) root.getScene().getWindow();
                ViewManager.showAccountView(stage, currentUser);
            } else {
                Stage stage = ViewManager.getPrimaryStage();
                if (stage == null) {
                    stage = new Stage();
                    ViewManager.setPrimaryStage(stage);
                }
                ViewManager.showAccountView(stage, currentUser);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ViewManager.showErrorAlert("Hata", "İşlem sırasında bir hata oluştu.");
        }
    }
}
