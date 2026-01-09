package com.example.restaurantmanagement;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ProfileController {

    @FXML private TextField profileNameField;
    @FXML private TextField profileEmailField;
    @FXML private TextField profileMobileField;
    @FXML private TextField profileAddressField;
    @FXML private Label profileStatusLabel;

    private UserDashboard dashboardController;
    private int currentUserId = -1;

    public void initData(UserDashboard dashboardController) {
        this.dashboardController = dashboardController;
        loadUserProfile();
    }

    private void loadUserProfile() {
        User user = DatabaseHelper.getUserByEmail(dashboardController.getCurrentUserEmail());
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

    @FXML
    protected void onBackClick() {
        dashboardController.onViewMenuClick();
    }
}
