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
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.File;

public class ItemDetailController {

    @FXML private ImageView detailImageView;
    @FXML private Label detailNameLabel;
    @FXML private Label detailCategoryLabel;
    @FXML private Label detailPriceLabel;
    @FXML private TextFlow detailDescTextFlow;
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
        
        Text descText = new Text(item.getDescription() != null ? item.getDescription() : "No description available.");
        descText.setFont(Font.font("Segoe UI", 14));
        detailDescTextFlow.getChildren().clear();
        detailDescTextFlow.getChildren().add(descText);
        
        detailImageView.setImage(null);
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            try {
                File file = new File(item.getImagePath());
                if (file.exists()) detailImageView.setImage(new Image(file.toURI().toString()));
            } catch (Exception e) {}
        }
        
        detailQuantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));
        detailStatusLabel.setText("");

        setupRatingDisplay();
    }

    private void setupRatingDisplay() {
        updateAverageRatingDisplay();

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
        
        Label prefix = new Label("Avg Rating: ");
        prefix.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14px;");
        container.getChildren().add(prefix);

        for (int i = 1; i <= 5; i++) {
            Label star = new Label("\u2605"); // Unicode Black Star
            star.setFont(new Font("Segoe UI Symbol", 24)); 
            
            if (i <= rating) {
                // Use inline style to force color
                star.setStyle("-fx-text-fill: gold; -fx-effect: dropshadow(one-pass-box, rgba(0,0,0,0.2), 2, 0, 0, 1);");
            } else if (i - rating < 1 && i - rating > 0) {
                // Partial star logic
                star.setStyle("-fx-text-fill: gold; -fx-effect: dropshadow(one-pass-box, rgba(0,0,0,0.2), 2, 0, 0, 1);");
            } else {
                star.setStyle("-fx-text-fill: lightgray; -fx-effect: dropshadow(one-pass-box, rgba(0,0,0,0.2), 2, 0, 0, 1);");
            }
            container.getChildren().add(star);
        }
        
        Label suffix = new Label();
        if (rating > 0) {
            suffix.setText(String.format(" (%.1f)", rating));
            suffix.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14px;");
        } else {
            suffix.setText(" (No ratings yet)");
            suffix.setStyle("-fx-text-fill: gray; -fx-font-style: italic; -fx-font-size: 12px;");
        }
        container.getChildren().add(suffix);
    }

    private void setupUserRatingStars(int currentRating) {
        userRatingBox.getChildren().clear();
        for (int i = 1; i <= 5; i++) {
            Label star = new Label("\u2605");
            star.setFont(new Font("Segoe UI Symbol", 32)); 
            
            // Initial state
            if (i <= currentRating) {
                star.setStyle("-fx-text-fill: gold; -fx-cursor: hand; -fx-effect: dropshadow(one-pass-box, rgba(0,0,0,0.2), 2, 0, 0, 1);");
            } else {
                star.setStyle("-fx-text-fill: lightgray; -fx-cursor: hand; -fx-effect: dropshadow(one-pass-box, rgba(0,0,0,0.2), 2, 0, 0, 1);");
            }
            
            final int rating = i;
            star.setOnMouseClicked(e -> {
                if (DatabaseHelper.addRating(dashboardController.getCurrentUserEmail(), detailMenuItem.getName(), rating)) {
                    ratingStatusLabel.setText("Thanks for your rating!");
                    ratingStatusLabel.setStyle("-fx-text-fill: green;");
                    setupUserRatingStars(rating); 
                    updateAverageRatingDisplay();
                } else {
                    ratingStatusLabel.setText("Failed to save rating.");
                    ratingStatusLabel.setStyle("-fx-text-fill: red;");
                }
            });
            
            star.setOnMouseEntered(e -> {
                for (int j = 0; j < 5; j++) {
                    Label s = (Label) userRatingBox.getChildren().get(j);
                    if (j < rating) {
                        s.setStyle("-fx-text-fill: gold; -fx-cursor: hand; -fx-effect: dropshadow(one-pass-box, rgba(0,0,0,0.2), 2, 0, 0, 1);");
                    } else {
                        s.setStyle("-fx-text-fill: lightgray; -fx-cursor: hand; -fx-effect: dropshadow(one-pass-box, rgba(0,0,0,0.2), 2, 0, 0, 1);");
                    }
                }
            });
            
            star.setOnMouseExited(e -> {
                int persistentRating = DatabaseHelper.getUserRating(dashboardController.getCurrentUserEmail(), detailMenuItem.getName());
                for (int j = 0; j < 5; j++) {
                    Label s = (Label) userRatingBox.getChildren().get(j);
                    if (j < persistentRating) {
                        s.setStyle("-fx-text-fill: gold; -fx-cursor: hand; -fx-effect: dropshadow(one-pass-box, rgba(0,0,0,0.2), 2, 0, 0, 1);");
                    } else {
                        s.setStyle("-fx-text-fill: lightgray; -fx-cursor: hand; -fx-effect: dropshadow(one-pass-box, rgba(0,0,0,0.2), 2, 0, 0, 1);");
                    }
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
