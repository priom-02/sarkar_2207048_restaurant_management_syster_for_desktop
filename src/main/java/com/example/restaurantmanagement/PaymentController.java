package com.example.restaurantmanagement;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

import java.util.Optional;

public class PaymentController {

    @FXML private Label paymentTotalLabel;
    @FXML private Label paymentStatusLabel;
    @FXML private ToggleGroup paymentGroup;
    @FXML private RadioButton cashRadio;
    @FXML private RadioButton cardRadio;
    @FXML private RadioButton onlineRadio;

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

    @FXML
    protected void onPlaceOrderClick() {
        RadioButton selectedRadio = (RadioButton) paymentGroup.getSelectedToggle();
        if (selectedRadio == null) {
            paymentStatusLabel.setText("Please select a payment method.");
            paymentStatusLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        
        String paymentMethod = selectedRadio.getText();
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Order");
        confirmation.setHeaderText("Place Order?");
        confirmation.setContentText("Are you sure you want to place this order using " + paymentMethod + "?");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            checkout(paymentMethod);
        } else {
            paymentStatusLabel.setText("Order cancelled.");
            paymentStatusLabel.setStyle("-fx-text-fill: orange;");
        }
    }

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
            
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Order Successful");
            success.setHeaderText(null);
            success.setContentText("Your order has been placed successfully via " + paymentMethod + "!");
            success.showAndWait();
            
            // Go back to the main menu after successful order
            dashboardController.onViewMenuClick();
            
        } else {
            paymentStatusLabel.setText("Some items failed to order. Please try again.");
            paymentStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    protected void onBackClick() {
        dashboardController.onViewCartClick();
    }
}
