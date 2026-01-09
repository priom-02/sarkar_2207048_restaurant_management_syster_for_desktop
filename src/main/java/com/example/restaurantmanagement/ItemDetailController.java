package com.example.restaurantmanagement;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;

public class ItemDetailController {

    @FXML private ImageView detailImageView;
    @FXML private Label detailNameLabel;
    @FXML private Label detailCategoryLabel;
    @FXML private Label detailPriceLabel;
    @FXML private Label detailDescLabel;
    @FXML private Spinner<Integer> detailQuantitySpinner;
    @FXML private Label detailStatusLabel;

    private UserDashboard dashboardController;
    private MenuItem detailMenuItem;

    public void initData(UserDashboard dashboardController, MenuItem item) {
        this.dashboardController = dashboardController;
        this.detailMenuItem = item;
        
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
        
        detailQuantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));
        detailStatusLabel.setText("");
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

    private void addToCart(MenuItem item, int quantity) {
        for (CartItem ci : dashboardController.getCart()) {
            if (ci.getItem().getId() == item.getId()) {
                ci.addQuantity(quantity);
                return;
            }
        }
        dashboardController.getCart().add(new CartItem(item, quantity));
    }

    @FXML
    protected void onBackClick() {
        dashboardController.onViewMenuClick();
    }
}
