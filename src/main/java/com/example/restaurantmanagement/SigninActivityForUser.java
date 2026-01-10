package com.example.restaurantmanagement;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
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
    
    @FXML private ProgressBar strengthBar;
    @FXML private Label strengthLabel;

    private boolean isPasswordAcceptable = false;

    @FXML
    public void initialize() {
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            checkPasswordStrength(newValue);
        });

        if (!DatabaseHelper.initializeDatabase()) {
            statusLabel.setText("DB Init Failed: " + DatabaseHelper.getLastError());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void checkPasswordStrength(String password) {
        int score = 0;
        
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()].*");
        boolean isLongEnough = password.length() >= 8;

        if (hasLower) score++;
        if (hasUpper) score++;
        if (hasDigit) score++;
        if (hasSpecial) score++;
        if (isLongEnough) score++;

        // *** THE FIX IS HERE ***
        // We now accept passwords that are "Good" (score 4) or "Strong" (score 5).
        isPasswordAcceptable = (score >= 4);

        switch (score) {
            case 0:
            case 1:
                strengthBar.setProgress(0.2);
                strengthBar.setStyle("-fx-accent: red;");
                strengthLabel.setText("Too Weak");
                break;
            case 2:
                strengthBar.setProgress(0.4);
                strengthBar.setStyle("-fx-accent: orange;");
                strengthLabel.setText("Weak");
                break;
            case 3:
                strengthBar.setProgress(0.6);
                strengthBar.setStyle("-fx-accent: #ffc107;"); // Yellow
                strengthLabel.setText("Medium");
                break;
            case 4:
                strengthBar.setProgress(0.8);
                strengthBar.setStyle("-fx-accent: #17a2b8;"); // Teal
                strengthLabel.setText("Good");
                break;
            case 5:
                strengthBar.setProgress(1.0);
                strengthBar.setStyle("-fx-accent: green;");
                strengthLabel.setText("Strong");
                break;
        }
    }

    @FXML
    protected void onRegisterButtonClick() {
        statusLabel.setText("");

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

        if (!isPasswordAcceptable) {
            statusLabel.setText("Password is not strong enough. Please improve it to at least 'Good'.");
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
            if (error != null && error.contains("UNIQUE constraint failed")) {
                statusLabel.setText("This email address is already registered.");
            } else {
                statusLabel.setText("Registration failed: " + (error != null ? error : "Unknown database error."));
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
        strengthBar.setProgress(0.0);
        strengthLabel.setText("");
    }
}
