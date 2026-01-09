package com.example.restaurantmanagement;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class CartController {

    @FXML private ListView<CartItem> cartListView;
    @FXML private Label cartTotalLabel;
    @FXML private Label cartStatusLabel;

    private UserDashboard dashboardController;

    public void initData(UserDashboard dashboardController) {
        this.dashboardController = dashboardController;
        refreshCartView();
    }

    @FXML
    public void initialize() {
        // Set a cell factory to display CartItem objects nicely
        cartListView.setCellFactory(lv -> new javafx.scene.control.ListCell<CartItem>() {
            @Override
            protected void updateItem(CartItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });
    }

    private void refreshCartView() {
        cartListView.setItems(FXCollections.observableArrayList(dashboardController.getCart()));
        
        double total = 0;
        for (CartItem ci : dashboardController.getCart()) {
            total += ci.getTotalPrice();
        }
        cartTotalLabel.setText(String.format("$%.2f", total));
        cartStatusLabel.setText("");
    }

    @FXML
    protected void onBackClick() {
        dashboardController.onViewMenuClick();
    }

    @FXML
    protected void onProceedToPaymentClick() {
        if (dashboardController.getCart().isEmpty()) {
            cartStatusLabel.setText("Cart is empty.");
            cartStatusLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        dashboardController.loadView("Payment.fxml");
    }
}
