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

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        // Ensure database is initialized when this screen is loaded
        if (!DatabaseHelper.initializeDatabase()) {
            if (statusLabel != null) {
                statusLabel.setText("DB Init Failed: " + DatabaseHelper.getLastError());
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }

    @FXML
    protected void onRegisterButtonClick() {
        String name = nameField != null ? nameField.getText() : "";
        String email = emailField != null ? emailField.getText() : "";
        String password = passwordField != null ? passwordField.getText() : "";

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            if (statusLabel != null) {
                statusLabel.setText("Please fill in all fields.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
            return;
        }

        if (DatabaseHelper.registerUser(name, email, password)) {
            if (statusLabel != null) {
                statusLabel.setText("Registration successful!");
                statusLabel.setStyle("-fx-text-fill: green;");
            }
            clearFields();
        } else {
            String error = DatabaseHelper.getLastError();
            if (statusLabel != null) {
                if (error != null && error.contains("UNIQUE constraint failed")) {
                    statusLabel.setText("Email already taken.");
                } else {
                    statusLabel.setText("Database Error: " + error);
                }
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }

    @FXML
    protected void onBackButtonClick() {
        try {
            Stage stage;
            if (nameField != null && nameField.getScene() != null) {
                stage = (Stage) nameField.getScene().getWindow();
            } else if (statusLabel != null && statusLabel.getScene() != null) {
                stage = (Stage) statusLabel.getScene().getWindow();
            } else {
                stage = new Stage();
            }

            FXMLLoader fxmlLoader = new FXMLLoader(UserLogin.class.getResource("UserLogin.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 400);
            stage.setTitle("Login - Restaurant Management");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        if (nameField != null) nameField.clear();
        if (emailField != null) emailField.clear();
        if (passwordField != null) passwordField.clear();
    }
}
