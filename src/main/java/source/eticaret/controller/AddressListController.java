package source.eticaret.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import source.eticaret.model.Address;
import source.eticaret.model.User;
import source.eticaret.view.ViewManager;

import java.io.IOException;
import java.util.List;

public class AddressListController {
    @FXML private VBox root;
    @FXML private VBox addressesContainer;
    
    private User currentUser;
    private List<Address> userAddresses;
    
    public void setUser(User user) {
        this.currentUser = user;
        loadAddresses();
    }
    
    private void loadAddresses() {
        addressesContainer.getChildren().clear();
        
        if (userAddresses == null || userAddresses.isEmpty()) {
            Label noAddressLabel = new Label("Kayıtlı adresiniz bulunmamaktadır.");
            noAddressLabel.setStyle("-fx-text-fill: #666; -fx-padding: 20;");
            addressesContainer.getChildren().add(noAddressLabel);
            return;
        }
        
        for (Address address : userAddresses) {
            addAddressCard(address);
        }
    }
    
    private void addAddressCard(Address address) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/templates/account/address_card.fxml"));
            Node card = loader.load();
            Label titleLabel = (Label) card.lookup("#addressTitle");
            Label addressLabel = (Label) card.lookup("#addressText");
            
            titleLabel.setText(address.getTitle());
            addressLabel.setText(address.getFullAddress());
            Button editButton = (Button) card.lookup("#editButton");
            editButton.setOnAction(e -> editAddress(address));
            Button deleteButton = (Button) card.lookup("#deleteButton");
            deleteButton.setOnAction(e -> deleteAddress(address));
            
            addressesContainer.getChildren().add(card);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void editAddress(Address address) {
        ViewManager.showAlert("Bilgi", "Bilgi", "Adres düzenleme ekranı açılacak: " + address.getTitle());
    }
    
    private void deleteAddress(Address address) {
        boolean confirmed = ViewManager.showConfirmation("Adresi Sil", 
            "Bu adresi silmek istediğinize emin misiniz?", 
            "Bu işlem geri alınamaz.");
        
        if (confirmed) {
            userAddresses.remove(address);
            loadAddresses();
        }
    }
    
    @FXML
    private void addNewAddress() {
        ViewManager.showAlert("Bilgi", "Bilgi", "Yeni adres ekleme ekranı açılacak");
    }
    
    @FXML
    private void goBack() {
        try {
            Stage stage = null;
            if (root != null && root.getScene() != null) {
                stage = (Stage) root.getScene().getWindow();
            }
            if (stage == null && addressesContainer != null && addressesContainer.getScene() != null) {
                stage = (Stage) addressesContainer.getScene().getWindow();
            }
            if (stage == null) {
                stage = ViewManager.getPrimaryStage();
                if (stage == null) {
                    throw new IllegalStateException("Could not determine the current stage");
                }
            }
            
            ViewManager.showAccountView(stage, currentUser);
        } catch (Exception e) {
            e.printStackTrace();
            ViewManager.showErrorAlert("Hata", "Geri dönülürken bir hata oluştu: " + e.getMessage());
        }
    }
}
