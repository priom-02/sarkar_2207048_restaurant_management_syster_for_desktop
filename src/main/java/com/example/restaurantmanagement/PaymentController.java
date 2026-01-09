package com.example.restaurantmanagement;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class PaymentController {

    @FXML private Label paymentTotalLabel;
    @FXML private Label paymentStatusLabel;

    private UserDashboard dashboardController;

    public void initData(UserDashboard dashboardController) {
        this.dashboardController = dashboardController;
        
        double total = 0;
        for (CartItem ci : dashboardController.getCart()) {
            total += ci.getTotalPrice();
        }
        paymentTotalLabel.setText(String.format("$%.2f", total));
        paymentStatusLabel.setText("");
    }

    @FXML protected void onCashOnDeliveryClick() { checkout("Cash on Delivery"); }
    @FXML protected void onByCardClick() { checkout("By Card"); }
    @FXML protected void onByOnlineClick() { checkout("By Online"); }

    private void checkout(String paymentMethod) {
        if (dashboardController.getCart().isEmpty()) {
            paymentStatusLabel.setText("Cart is empty.");
            paymentStatusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        boolean allSuccess = true;
        for (CartItem ci : dashboardController.getCart()) {
            if (!DatabaseHelper.placeOrder(dashboardController.getCurrentUserEmail(), ci.getItem().getName(), ci.getQuantity(), ci.getTotalPrice())) {
                allSuccess = false;
            }
        }

        if (allSuccess) {
            dashboardController.getCart().clear();
            paymentStatusLabel.setText("Order placed successfully via " + paymentMethod + "!");
            paymentStatusLabel.setStyle("-fx-text-fill: green;");
        } else {
            paymentStatusLabel.setText("Some items failed to order.");
            paymentStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    protected void onBackClick() {
        dashboardController.onViewCartClick();
    }
}
