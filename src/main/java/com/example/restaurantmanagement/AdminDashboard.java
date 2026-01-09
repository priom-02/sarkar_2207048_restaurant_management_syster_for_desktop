package com.example.restaurantmanagement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class AdminDashboard {

    // Views
    @FXML private VBox viewMenuView;
    @FXML private VBox addItemView;
    @FXML private VBox viewOrdersView;

    // View Menu / Edit Components
    @FXML private ListView<MenuItem> menuListView;
    @FXML private TextField editNameField;
    @FXML private TextField editCategoryField;
    @FXML private TextField editPriceField;
    @FXML private ComboBox<String> editStatusComboBox;
    @FXML private TextArea editDescriptionField;
    @FXML private Label editImagePathLabel;
    @FXML private Label editStatusLabel;
    
    // Add Item Components
    @FXML private TextField addNameField;
    @FXML private TextField addCategoryField;
    @FXML private TextField addPriceField;
    @FXML private ComboBox<String> addStatusComboBox;
    @FXML private TextArea addDescriptionField;
    @FXML private Label addImagePathLabel;
    @FXML private Label addStatusLabel;

    // View Orders Components
    @FXML private ListView<String> allOrdersListView;

    private File selectedImageFile = null;

    @FXML
    public void initialize() {
        // Initialize ComboBoxes
        ObservableList<String> statuses = FXCollections.observableArrayList("Available", "Out of Stock");
        if (editStatusComboBox != null) editStatusComboBox.setItems(statuses);
        if (addStatusComboBox != null) {
            addStatusComboBox.setItems(statuses);
            addStatusComboBox.setValue("Available");
        }

        refreshMenuItems();
        
        // Default View
        showView(viewMenuView);
    }

    // Navigation Handlers
    @FXML protected void onViewMenuClick() { showView(viewMenuView); refreshMenuItems(); }
    @FXML protected void onAddItemClick() { showView(addItemView); clearAddFields(); }
    @FXML protected void onViewOrdersClick() { showView(viewOrdersView); refreshOrders(); }

    @FXML
    protected void onLogoutClick() {
        try {
            Stage stage = (Stage) viewMenuView.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(UserLogin.class.getResource("UserLogin.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 400);
            stage.setTitle("Login - Restaurant Management");
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showView(VBox view) {
        viewMenuView.setVisible(false);
        addItemView.setVisible(false);
        viewOrdersView.setVisible(false);
        view.setVisible(true);
        view.toFront();
    }

    // --- View Menu / Edit Logic ---

    private void refreshMenuItems() {
        if (menuListView == null) return;
        List<MenuItem> items = DatabaseHelper.getAllMenuItems();
        menuListView.setItems(FXCollections.observableArrayList(items));
        menuListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(MenuItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.getName() + " (" + item.getCategory() + ") - $" + item.getPrice() + " [" + item.getStatus() + "]");
            }
        });

        menuListView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                editNameField.setText(newV.getName());
                editCategoryField.setText(newV.getCategory());
                editPriceField.setText(String.valueOf(newV.getPrice()));
                editStatusComboBox.setValue(newV.getStatus());
                editDescriptionField.setText(newV.getDescription());
                editImagePathLabel.setText(newV.getImagePath() == null ? "No image" : newV.getImagePath());
                selectedImageFile = null;
            }
        });
    }

    @FXML
    protected void onEditChooseImageClick() {
        File file = chooseImage();
        if (file != null) {
            selectedImageFile = file;
            editImagePathLabel.setText(file.getName());
        }
    }

    @FXML
    protected void onUpdateClick() {
        MenuItem sel = menuListView.getSelectionModel().getSelectedItem();
        if (sel == null) { editStatusLabel.setText("Select an item to update."); return; }
        try {
            String name = editNameField.getText();
            String category = editCategoryField.getText();
            double price = Double.parseDouble(editPriceField.getText());
            String status = editStatusComboBox.getValue();
            String desc = editDescriptionField.getText();
            String storedPath = sel.getImagePath();
            
            if (selectedImageFile != null) {
                storedPath = DatabaseHelper.storeImageFile(Path.of(selectedImageFile.getAbsolutePath()));
            }
            
            if (DatabaseHelper.updateMenuItem(sel.getId(), name, category, price, status, desc, storedPath)) {
                editStatusLabel.setText("Updated successfully.");
                editStatusLabel.setStyle("-fx-text-fill: green;");
                refreshMenuItems();
            } else {
                editStatusLabel.setText("Update failed: " + DatabaseHelper.getLastError());
                editStatusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (NumberFormatException e) {
            editStatusLabel.setText("Invalid price.");
            editStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    protected void onDeleteClick() {
        MenuItem sel = menuListView.getSelectionModel().getSelectedItem();
        if (sel == null) { editStatusLabel.setText("Select an item to delete."); return; }
        if (DatabaseHelper.deleteMenuItem(sel.getId())) {
            editStatusLabel.setText("Deleted successfully.");
            editStatusLabel.setStyle("-fx-text-fill: green;");
            refreshMenuItems();
            clearEditFields();
        } else {
            editStatusLabel.setText("Delete failed: " + DatabaseHelper.getLastError());
            editStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void clearEditFields() {
        editNameField.clear();
        editCategoryField.clear();
        editPriceField.clear();
        editDescriptionField.clear();
        editImagePathLabel.setText("No image");
        selectedImageFile = null;
    }

    // --- Add Item Logic ---

    @FXML
    protected void onAddChooseImageClick() {
        File file = chooseImage();
        if (file != null) {
            selectedImageFile = file;
            addImagePathLabel.setText(file.getName());
        }
    }

    @FXML
    protected void onAddClick() {
        try {
            String name = addNameField.getText();
            String category = addCategoryField.getText();
            double price = Double.parseDouble(addPriceField.getText());
            String status = addStatusComboBox.getValue();
            String desc = addDescriptionField.getText();
            String storedPath = null;
            
            if (selectedImageFile != null) {
                storedPath = DatabaseHelper.storeImageFile(Path.of(selectedImageFile.getAbsolutePath()));
            }
            
            if (DatabaseHelper.addMenuItem(name, category, price, status, desc, storedPath)) {
                addStatusLabel.setText("Item added successfully.");
                addStatusLabel.setStyle("-fx-text-fill: green;");
                clearAddFields();
            } else {
                addStatusLabel.setText("Add failed: " + DatabaseHelper.getLastError());
                addStatusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (NumberFormatException e) {
            addStatusLabel.setText("Invalid price.");
            addStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void clearAddFields() {
        addNameField.clear();
        addCategoryField.clear();
        addPriceField.clear();
        addDescriptionField.clear();
        addStatusComboBox.setValue("Available");
        addImagePathLabel.setText("No image selected");
        selectedImageFile = null;
    }

    // --- View Orders Logic ---

    @FXML
    protected void onRefreshOrdersClick() {
        refreshOrders();
    }

    private void refreshOrders() {
        if (allOrdersListView == null) return;
        List<String> orders = DatabaseHelper.getAllOrders();
        allOrdersListView.setItems(FXCollections.observableArrayList(orders));
    }

    // --- Helpers ---

    private File chooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Food Image");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        Stage stage = (Stage) viewMenuView.getScene().getWindow(); // Use any node to get stage
        return chooser.showOpenDialog(stage);
    }
}
