package source.eticaret.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import source.eticaret.model.Category;
import source.eticaret.service.CategoryService;
import source.eticaret.service.DatabaseService;

import java.util.List;
import java.util.Optional;

public class CategoryController {
    private final CategoryService categoryService;

    @FXML private TableView<Category> categoryTable;
    @FXML private TableColumn<Category, Long> idColumn;
    @FXML private TableColumn<Category, String> nameColumn;
    @FXML private TableColumn<Category, String> descriptionColumn;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<Category> parentCategoryCombo;
    @FXML private Button saveButton;
    @FXML private Button deleteButton;
    @FXML private VBox formContainer;

    private Category currentCategory;

    public CategoryController() {
        this.categoryService = new CategoryService(DatabaseService.getInstance());
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
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        

        categoryTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    loadCategoryDetails(newSelection);
                }
            });
    }

    private void loadCategories() {
        List<Category> categories = categoryService.getAllCategories();
        categoryTable.getItems().setAll(categories);
        

        parentCategoryCombo.getItems().clear();
        parentCategoryCombo.getItems().add(null);
        parentCategoryCombo.getItems().addAll(categoryService.getRootCategories());
    }

    private void loadCategoryDetails(Category category) {
        currentCategory = category;
        nameField.setText(category.getName());
        descriptionArea.setText(category.getDescription());
        

        if (category.getParentId() != null) {
            Optional<Category> parent = categoryService.getCategoryById(category.getParentId());
            parent.ifPresent(parentCategoryCombo::setValue);
        } else {
            parentCategoryCombo.setValue(null);
        }
        
        saveButton.setText("Güncelle");
        deleteButton.setDisable(false);
        formContainer.setDisable(false);
    }

    private void setupFormValidation() {

        saveButton.disableProperty().bind(
            nameField.textProperty().isEmpty()
        );
    }

    @FXML
    private void handleSave() {
        if (currentCategory == null) {

            Category parent = parentCategoryCombo.getValue();
            Category newCategory = categoryService.createCategory(
                nameField.getText(),
                descriptionArea.getText(),
                parent != null ? parent.getId() : null
            );
            
            if (newCategory != null) {
                showAlert("Başarılı", "Kategori başarıyla oluşturuldu.", Alert.AlertType.INFORMATION);
                clearForm();
                loadCategories();
            } else {
                showAlert("Hata", "Kategori oluşturulurken bir hata oluştu.", Alert.AlertType.ERROR);
            }
        } else {

            Category parent = parentCategoryCombo.getValue();
            Category updatedCategory = categoryService.updateCategory(
                currentCategory.getId(),
                nameField.getText(),
                descriptionArea.getText(),
                parent != null ? parent.getId() : null
            );
            
            if (updatedCategory != null) {
                showAlert("Başarılı", "Kategori başarıyla güncellendi.", Alert.AlertType.INFORMATION);
                clearForm();
                loadCategories();
            } else {
                showAlert("Hata", "Kategori güncellenirken bir hata oluştu.", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleDelete() {
        if (currentCategory != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Kategori Sil");
            alert.setHeaderText("Kategori Silinecek");
            alert.setContentText("Bu kategoriyi silmek istediğinizden emin misiniz?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    if (categoryService.deleteCategory(currentCategory.getId())) {
                        showAlert("Başarılı", "Kategori başarıyla silindi.", Alert.AlertType.INFORMATION);
                        clearForm();
                        loadCategories();
                    } else {
                        showAlert("Hata", "Kategori silinirken bir hata oluştu.", Alert.AlertType.ERROR);
                    }
                } catch (Exception e) {
                    showAlert("Hata", e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        }
    }

    @FXML
    private void handleNewCategory() {
        currentCategory = null;
        clearForm();
        formContainer.setDisable(false);
        nameField.requestFocus();
    }

    private void clearForm() {
        currentCategory = null;
        nameField.clear();
        descriptionArea.clear();
        parentCategoryCombo.setValue(null);
        saveButton.setText("Kaydet");
        deleteButton.setDisable(true);
        formContainer.setDisable(true);
        categoryTable.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
