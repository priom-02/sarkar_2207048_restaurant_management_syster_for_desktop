package com.example.restaurantmanagement;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.File;

public class ItemDetailController {

    @FXML private ImageView detailImageView;
    @FXML private Label detailNameLabel;
    @FXML private Label detailCategoryLabel;
    @FXML private Label detailPriceLabel;
    @FXML private Label detailDescLabel;
    @FXML private Spinner<Integer> detailQuantitySpinner;
    @FXML private Label detailStatusLabel;

    // Rating UI
    @FXML private HBox averageRatingBox;
    @FXML private VBox ratingSection;
    @FXML private HBox userRatingBox;
    @FXML private Label ratingStatusLabel;

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

        // Initialize rating display
        setupRatingDisplay();
    }

    private void setupRatingDisplay() {
        // 1. Display Average Rating
        updateAverageRatingDisplay();

        // 2. Check if user can rate
        boolean hasOrdered = DatabaseHelper.hasUserOrderedItem(dashboardController.getCurrentUserEmail(), detailMenuItem.getName());
        
        if (hasOrdered) {
            ratingSection.setVisible(true);
            int userPreviousRating = DatabaseHelper.getUserRating(dashboardController.getCurrentUserEmail(), detailMenuItem.getName());
            if (userPreviousRating > 0) {
                ratingStatusLabel.setText("You rated this item " + userPreviousRating + " stars.");
            } else {
                ratingStatusLabel.setText("You can rate this item!");
            }
            setupUserRatingStars(userPreviousRating);
        } else {
            ratingSection.setVisible(false);
        }
    }

    private void updateAverageRatingDisplay() {
        double avgRating = DatabaseHelper.getAverageRating(detailMenuItem.getName());
        displayStars(averageRatingBox, avgRating);
    }

    private void displayStars(HBox container, double rating) {
        container.getChildren().clear();
        for (int i = 1; i <= 5; i++) {
            Label star = new Label("★");
            star.setFont(new Font(20));
            if (i <= rating) {
                star.setTextFill(Color.GOLD);
            } else if (i - rating < 1) {
                // Half star logic (simulated with color)
                star.setTextFill(Color.GOLD); 
            } else {
                star.setTextFill(Color.LIGHTGRAY);
            }
            container.getChildren().add(star);
        }
        if (rating > 0) {
            container.getChildren().add(new Label(String.format(" (%.1f)", rating)));
        } else {
            container.getChildren().add(new Label(" (No ratings yet)"));
        }
    }

    private void setupUserRatingStars(int currentRating) {
        userRatingBox.getChildren().clear();
        for (int i = 1; i <= 5; i++) {
            Label star = new Label("★");
            star.setFont(new Font(24)); // Slightly larger for interaction
            star.setTextFill(i <= currentRating ? Color.GOLD : Color.LIGHTGRAY);
            
            final int rating = i;
            star.setOnMouseClicked(e -> {
                if (DatabaseHelper.addRating(dashboardController.getCurrentUserEmail(), detailMenuItem.getName(), rating)) {
                    ratingStatusLabel.setText("Thanks for your rating!");
                    ratingStatusLabel.setStyle("-fx-text-fill: green;");
                    
                    // Update the user's stars visual immediately
                    setupUserRatingStars(rating); 
                    
                    // Update the average rating display immediately
                    updateAverageRatingDisplay();
                } else {
                    ratingStatusLabel.setText("Failed to save rating.");
                    ratingStatusLabel.setStyle("-fx-text-fill: red;");
                }
            });
            
            star.setOnMouseEntered(e -> {
                for (int j = 0; j < 5; j++) {
                    Label s = (Label) userRatingBox.getChildren().get(j);
                    s.setTextFill(j < rating ? Color.GOLD : Color.LIGHTGRAY);
                }
            });
            
            star.setOnMouseExited(e -> {
                // Re-fetch the persistent rating to reset the view correctly
                int persistentRating = DatabaseHelper.getUserRating(dashboardController.getCurrentUserEmail(), detailMenuItem.getName());
                for (int j = 0; j < 5; j++) {
                    Label s = (Label) userRatingBox.getChildren().get(j);
                    s.setTextFill(j < persistentRating ? Color.GOLD : Color.LIGHTGRAY);
                }
            });
            
            userRatingBox.getChildren().add(star);
        }
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
