package com.example.restaurantmanagement;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ViewMenuController {

    @FXML private HBox categoryBox;
    @FXML private TilePane menuTilePane;
    @FXML private Label selectedItemLabel;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Label statusLabel;

    private UserDashboard dashboardController;
    private MenuItem selectedMenuItem;
    private String currentCategoryFilter = "All Items";

    public void initData(UserDashboard dashboardController) {
        this.dashboardController = dashboardController;
        refreshMenu();
    }

    @FXML
    public void initialize() {
        if (quantitySpinner != null) {
            quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, 0));
            quantitySpinner.setDisable(true);
        }
    }

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
                dashboardController.showItemDetail(item);
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

    @FXML
    protected void onAddToCartClick() {
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
        for (CartItem ci : dashboardController.getCart()) {
            if (ci.getItem().getId() == item.getId()) {
                ci.addQuantity(quantity);
                return;
            }
        }
        dashboardController.getCart().add(new CartItem(item, quantity));
    }
}
