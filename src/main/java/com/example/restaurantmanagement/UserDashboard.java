package com.example.restaurantmanagement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UserDashboard {

    @FXML private HBox categoryBox;
    @FXML private TilePane menuTilePane;
    @FXML private Label selectedItemLabel;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Label statusLabel;
    @FXML private ListView<String> orderHistoryListView;

    // Views
    @FXML private VBox viewMenuView;
    @FXML private VBox orderHistoryView;
    @FXML private VBox profileView;
    @FXML private VBox cartView;
    @FXML private VBox itemDetailView;

    // Cart Components
    @FXML private ListView<String> cartListView;
    @FXML private Label cartTotalLabel;
    @FXML private Label cartStatusLabel;

    // Item Detail Components
    @FXML private ImageView detailImageView;
    @FXML private Label detailNameLabel;
    @FXML private Label detailCategoryLabel;
    @FXML private Label detailPriceLabel;
    @FXML private Label detailDescLabel;
    @FXML private Spinner<Integer> detailQuantitySpinner;
    @FXML private Label detailStatusLabel;

    // Profile fields
    @FXML private TextField profileNameField;
    @FXML private TextField profileEmailField;
    @FXML private TextField profileMobileField;
    @FXML private TextField profileAddressField;
    @FXML private Label profileStatusLabel;

    private String currentUserEmail = "user@example.com"; 
    private int currentUserId = -1;
    private MenuItem selectedMenuItem = null;
    private MenuItem detailMenuItem = null;
    private String currentCategoryFilter = "All Items";
    
    // In-memory cart: List of CartItem objects (inner class)
    private List<CartItem> cart = new ArrayList<>();

    public void setCurrentUserEmail(String email) {
        this.currentUserEmail = email;
    }

    @FXML
    public void initialize() {
        if (quantitySpinner != null) {
            quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, 0));
            quantitySpinner.setDisable(true);
        }
        if (detailQuantitySpinner != null) {
            detailQuantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));
        }
        
        refreshMenu();
        refreshOrderHistory();
        
        // Default view
        showView(viewMenuView);
    }

    @FXML protected void onViewMenuClick() { showView(viewMenuView); }
    @FXML protected void onOrderHistoryClick() { showView(orderHistoryView); refreshOrderHistory(); }
    @FXML protected void onProfileClick() { showView(profileView); loadUserProfile(); }
    
    @FXML protected void onViewCartClick() { 
        showView(cartView); 
        refreshCartView();
    }

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

    private void showView(VBox viewToShow) {
        viewMenuView.setVisible(false);
        orderHistoryView.setVisible(false);
        profileView.setVisible(false);
        cartView.setVisible(false);
        itemDetailView.setVisible(false);
        
        viewToShow.setVisible(true);
        viewToShow.toFront();
    }

    // --- Menu Logic ---

    private void refreshMenu() {
        if (menuTilePane == null) return;
        
        List<MenuItem> allItems = DatabaseHelper.getAllMenuItems();
        allItems.removeIf(item -> !"Available".equalsIgnoreCase(item.getStatus()));
        
        setupCategories(allItems);
        
        List<MenuItem> displayedItems;
        if ("All Items".equals(currentCategoryFilter)) {
            displayedItems = allItems;
        } else {
            displayedItems = allItems.stream()
                    .filter(item -> currentCategoryFilter.equalsIgnoreCase(item.getCategory()))
                    .collect(Collectors.toList());
        }
        
        menuTilePane.getChildren().clear();
        for (MenuItem item : displayedItems) {
            VBox card = createMenuItemCard(item);
            menuTilePane.getChildren().add(card);
        }
    }

    private void setupCategories(List<MenuItem> items) {
        if (categoryBox == null) return;
        Set<String> categories = new HashSet<>();
        categories.add("All Items");
        for (MenuItem item : items) {
            if (item.getCategory() != null && !item.getCategory().isEmpty()) {
                categories.add(item.getCategory());
            }
        }
        categoryBox.getChildren().clear();
        addCategoryButton("All Items");
        categories.remove("All Items");
        categories.stream().sorted().forEach(this::addCategoryButton);
    }

    private void addCategoryButton(String categoryName) {
        Button btn = new Button(categoryName);
        if (categoryName.equals(currentCategoryFilter)) {
            btn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15;");
        } else {
            btn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: black; -fx-background-radius: 15;");
        }
        btn.setOnAction(e -> {
            currentCategoryFilter = categoryName;
            refreshMenu();
        });
        categoryBox.getChildren().add(btn);
    }

    private VBox createMenuItemCard(MenuItem item) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");
        card.setPrefWidth(150);
        card.setAlignment(Pos.CENTER);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(130);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            try {
                File file = new File(item.getImagePath());
                if (file.exists()) imageView.setImage(new Image(file.toURI().toString()));
            } catch (Exception e) {}
        }
        
        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label categoryLabel = new Label(item.getCategory());
        categoryLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 12px;");
        Label priceLabel = new Label("$" + item.getPrice());
        priceLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");

        card.getChildren().addAll(imageView, nameLabel, categoryLabel, priceLabel);
        
        card.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                showItemDetail(item);
            } else {
                selectedMenuItem = item;
                selectedItemLabel.setText(item.getName() + " ($" + item.getPrice() + ")");
                quantitySpinner.setDisable(false);
                quantitySpinner.getValueFactory().setValue(0);
                menuTilePane.getChildren().forEach(node -> node.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);"));
                card.setStyle("-fx-background-color: #e6f7ff; -fx-border-color: #1890ff; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");
            }
        });
        
        return card;
    }

    // --- Item Detail Logic ---

    private void showItemDetail(MenuItem item) {
        detailMenuItem = item;
        detailNameLabel.setText(item.getName());
        detailCategoryLabel.setText(item.getCategory());
        detailPriceLabel.setText("$" + item.getPrice());
        detailDescLabel.setText(item.getDescription() != null ? item.getDescription() : "No description available.");
        
        detailImageView.setImage(null);
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            try {
                File file = new File(item.getImagePath());
                if (file.exists()) detailImageView.setImage(new Image(file.toURI().toString()));
            } catch (Exception e) {}
        }
        
        detailQuantitySpinner.getValueFactory().setValue(1);
        detailStatusLabel.setText("");
        showView(itemDetailView);
    }

    @FXML
    protected void onDetailAddToCartClick() {
        if (detailMenuItem == null) return;
        int qty = detailQuantitySpinner.getValue();
        if (qty > 0) {
            addToCart(detailMenuItem, qty);
            detailStatusLabel.setText("Added to cart!");
            detailStatusLabel.setStyle("-fx-text-fill: green;");
        }
    }

    // --- Cart Logic ---

    @FXML
    protected void onAddToOrderClick() {
        if (selectedMenuItem == null) {
            statusLabel.setText("Please select an item.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        int qty = quantitySpinner.getValue();
        if (qty <= 0) {
            statusLabel.setText("Quantity must be > 0.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        addToCart(selectedMenuItem, qty);
        statusLabel.setText("Added to cart!");
        statusLabel.setStyle("-fx-text-fill: green;");
        quantitySpinner.getValueFactory().setValue(0);
    }

    private void addToCart(MenuItem item, int quantity) {
        // Check if item already in cart
        for (CartItem ci : cart) {
            if (ci.item.getId() == item.getId()) {
                ci.quantity += quantity;
                return;
            }
        }
        cart.add(new CartItem(item, quantity));
    }

    private void refreshCartView() {
        ObservableList<String> items = FXCollections.observableArrayList();
        double total = 0;
        for (CartItem ci : cart) {
            double itemTotal = ci.item.getPrice() * ci.quantity;
            total += itemTotal;
            items.add(String.format("%s x%d - $%.2f", ci.item.getName(), ci.quantity, itemTotal));
        }
        cartListView.setItems(items);
        cartTotalLabel.setText(String.format("$%.2f", total));
        cartStatusLabel.setText("");
    }

    @FXML
    protected void onCheckoutClick() {
        if (cart.isEmpty()) {
            cartStatusLabel.setText("Cart is empty.");
            cartStatusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        boolean allSuccess = true;
        for (CartItem ci : cart) {
            double total = ci.item.getPrice() * ci.quantity;
            if (!DatabaseHelper.placeOrder(currentUserEmail, ci.item.getName(), ci.quantity, total)) {
                allSuccess = false;
            }
        }

        if (allSuccess) {
            cart.clear();
            refreshCartView();
            cartStatusLabel.setText("Order placed successfully!");
            cartStatusLabel.setStyle("-fx-text-fill: green;");
            refreshOrderHistory();
        } else {
            cartStatusLabel.setText("Some items failed to order.");
            cartStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    // --- Other Logic ---

    @FXML
    protected void onRefreshHistoryClick() {
        refreshOrderHistory();
    }

    private void refreshOrderHistory() {
        if (orderHistoryListView == null) return;
        List<String> history = DatabaseHelper.getOrderHistory(currentUserEmail);
        orderHistoryListView.setItems(FXCollections.observableArrayList(history));
    }

    private void loadUserProfile() {
        User user = DatabaseHelper.getUserByEmail(currentUserEmail);
        if (user != null) {
            currentUserId = user.getId();
            profileNameField.setText(user.getName());
            profileEmailField.setText(user.getEmail());
            profileMobileField.setText(user.getMobile() != null ? user.getMobile() : "");
            profileAddressField.setText(user.getAddress() != null ? user.getAddress() : "");
            profileStatusLabel.setText("");
        } else {
            profileStatusLabel.setText("Failed to load user data.");
            profileStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    protected void onUpdateProfileClick() {
        if (currentUserId == -1) {
            profileStatusLabel.setText("User not loaded.");
            return;
        }
        String newName = profileNameField.getText();
        String newMobile = profileMobileField.getText();
        String newAddress = profileAddressField.getText();
        if (newName.isEmpty()) {
            profileStatusLabel.setText("Name cannot be empty.");
            profileStatusLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        if (DatabaseHelper.updateUserProfile(currentUserId, newName, newMobile, newAddress)) {
            profileStatusLabel.setText("Profile updated successfully!");
            profileStatusLabel.setStyle("-fx-text-fill: green;");
        } else {
            profileStatusLabel.setText("Update failed: " + DatabaseHelper.getLastError());
            profileStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    // Inner class for Cart
    private static class CartItem {
        MenuItem item;
        int quantity;
        public CartItem(MenuItem item, int quantity) {
            this.item = item;
            this.quantity = quantity;
        }
    }
}
