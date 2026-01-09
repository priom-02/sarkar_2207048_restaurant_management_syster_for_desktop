package com.example.restaurantmanagement;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class SigninActivityForUser {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField mobileField;
    @FXML private TextField addressField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        // Ensure database is initialized when this screen is loaded
        if (!DatabaseHelper.initializeDatabase()) {
            statusLabel.setText("DB Init Failed: " + DatabaseHelper.getLastError());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    protected void onRegisterButtonClick() {
        String name = nameField.getText();
        String email = emailField.getText();
        String mobile = mobileField.getText();
        String address = addressField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            statusLabel.setText("Please fill in all required fields.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (!password.equals(confirmPassword)) {
            statusLabel.setText("Passwords do not match.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (DatabaseHelper.registerUser(name, email, password, mobile, address)) {
            statusLabel.setText("Registration successful!");
            statusLabel.setStyle("-fx-text-fill: green;");
            clearFields();
        } else {
            String error = DatabaseHelper.getLastError();
            if (error.contains("UNIQUE constraint failed")) {
                statusLabel.setText("Email already taken.");
            } else {
                statusLabel.setText("Database Error: " + error);
            }
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    protected void onBackButtonClick() {
        try {
            Stage stage = (Stage) nameField.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(UserLogin.class.getResource("UserLogin.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 400);
            stage.setTitle("Login - Restaurant Management");
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        nameField.clear();
        emailField.clear();
        mobileField.clear();
        addressField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }
}
