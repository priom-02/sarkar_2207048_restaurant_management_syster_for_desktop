package com.example.restaurantmanagement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    @FXML private ListView<Order> ordersListView;

    private File selectedImageFile = null;

    @FXML
    public void initialize() {
        ObservableList<String> statuses = FXCollections.observableArrayList("Available", "Out of Stock");
        if (editStatusComboBox != null) editStatusComboBox.setItems(statuses);
        if (addStatusComboBox != null) {
            addStatusComboBox.setItems(statuses);
            addStatusComboBox.setValue("Available");
        }

        refreshMenuItems();
        
        if (ordersListView != null) {
            ordersListView.setCellFactory(lv -> new OrderCardCell());
        }
        
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

    private void refreshOrders() {
        if (ordersListView == null) return;
        List<Order> groupedOrders = getGroupedOrdersFromDB();
        ordersListView.setItems(FXCollections.observableArrayList(groupedOrders));
    }

    private List<Order> getGroupedOrdersFromDB() {
        Map<String, Order> groupedOrdersMap = new LinkedHashMap<>();
        List<Order> allOrders = DatabaseHelper.getAllOrdersWithDetails();

        if (allOrders == null) return new ArrayList<>();

        for (Order order : allOrders) {
            String transactionId = order.getTransactionId();
            if (transactionId == null) continue;

            Order groupedOrder = groupedOrdersMap.get(transactionId);
            if (groupedOrder == null) {
                groupedOrder = new Order(
                    order.getId(), order.getUserEmail(), order.getItemName() + " x" + order.getQuantity(), 
                    order.getQuantity(), order.getTotalPrice(), order.getOrderDate(), order.getStatus(),
                    order.getUserName(), order.getUserMobile(), order.getUserAddress()
                );
                groupedOrder.setTransactionId(transactionId);
                groupedOrdersMap.put(transactionId, groupedOrder);
            } else {
                groupedOrder.setItemName(groupedOrder.getItemName() + "\n" + order.getItemName() + " x" + order.getQuantity());
                groupedOrder.setTotalPrice(groupedOrder.getTotalPrice() + order.getTotalPrice());
            }
        }
        return new ArrayList<>(groupedOrdersMap.values());
    }

    // --- Helpers ---

    private File chooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Food Image");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        Stage stage = (Stage) viewMenuView.getScene().getWindow();
        return chooser.showOpenDialog(stage);
    }
    
    // --- Inner Class for Order Card ---
    
    private class OrderCardCell extends ListCell<Order> {
        private VBox card = new VBox(10);
        private Label orderIdLabel = new Label();
        private Label userDetailsLabel = new Label();
        private Label orderDetailsLabel = new Label();
        private Label totalLabel = new Label();
        private Label statusLabel = new Label();
        private Button acceptButton = new Button("Accept");
        private Button removeButton = new Button("Remove");
        private HBox buttonBox = new HBox(10, acceptButton, removeButton);

        public OrderCardCell() {
            super();
            card.setPadding(new Insets(10));
            card.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-background-radius: 5;");
            
            orderIdLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            totalLabel.setStyle("-fx-font-weight: bold;");
            statusLabel.setStyle("-fx-font-weight: bold;");
            
            card.getChildren().addAll(orderIdLabel, userDetailsLabel, orderDetailsLabel, totalLabel, statusLabel, buttonBox);
        }

        @Override
        protected void updateItem(Order order, boolean empty) {
            super.updateItem(order, empty);
            if (empty || order == null) {
                setGraphic(null);
            } else {
                orderIdLabel.setText("Transaction ID: " + order.getTransactionId().substring(0, 8) + "...");
                userDetailsLabel.setText("User: " + order.getUserName() + " | Mobile: " + order.getUserMobile() + "\nAddress: " + order.getUserAddress());
                
                String[] items = order.getItemName().split("\n");
                StringBuilder itemText = new StringBuilder("Items:\n");
                for(String item : items) {
                    itemText.append("  - ").append(item).append("\n");
                }
                orderDetailsLabel.setText(itemText.toString());
                
                totalLabel.setText(String.format("Total: $%.2f", order.getTotalPrice()));
                statusLabel.setText("Status: " + order.getStatus());
                
                switch (order.getStatus().toLowerCase()) {
                    case "accepted":
                        statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        break;
                    case "removed":
                        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        break;
                    default:
                        statusLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                        break;
                }

                acceptButton.setOnAction(e -> handleUpdateStatus(order, "Accepted"));
                removeButton.setOnAction(e -> handleUpdateStatus(order, "Removed"));
                
                setGraphic(card);
            }
        }
        
        private void handleUpdateStatus(Order order, String newStatus) {
            if (DatabaseHelper.updateOrderStatusByTransaction(order.getTransactionId(), newStatus)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Order Updated");
                alert.setHeaderText(null);
                alert.setContentText("Order for transaction " + order.getTransactionId().substring(0, 8) + "... has been " + newStatus.toLowerCase() + ".");
                alert.showAndWait();
                
                Alert userAlert = new Alert(Alert.AlertType.INFORMATION);
                userAlert.setTitle("Your Order Status");
                userAlert.setHeaderText("Update on your recent order");
                userAlert.setContentText("Your order (ID: " + order.getTransactionId().substring(0, 8) + "...) has been " + newStatus.toLowerCase() + " by the restaurant.");
                userAlert.show();
                
                refreshOrders();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Update Failed");
                alert.setContentText("Failed to update order status.");
                alert.showAndWait();
            }
        }
    }
}
