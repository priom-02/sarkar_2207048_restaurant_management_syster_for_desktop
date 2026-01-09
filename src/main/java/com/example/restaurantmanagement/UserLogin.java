package com.example.restaurantmanagement;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class UserLogin extends Application {

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private TextField usernameField; // Changed back to usernameField to match FXML

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    @Override
    public void start(Stage stage) throws IOException {
        DatabaseHelper.initializeDatabase(); // Initialize DB on startup
        FXMLLoader fxmlLoader = new FXMLLoader(UserLogin.class.getResource("UserLogin.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 400);
        stage.setTitle("Login - Restaurant Management");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    protected void onLoginButtonClick() {
        String role = roleComboBox != null ? roleComboBox.getValue() : null;
        String email = usernameField != null ? usernameField.getText() : "";
        String password = passwordField != null ? passwordField.getText() : "";

        if (role == null) {
            if (statusLabel != null) statusLabel.setText("Please select a role.");
            return;
        }

        if (email.isEmpty() || password.isEmpty()) {
            if (statusLabel != null) statusLabel.setText("Please enter email and password.");
            return;
        }

        if ("Admin".equals(role)) {
            handleAdminLogin(email, password);
        } else if ("User".equals(role)) {
            handleUserLogin(email, password);
        }
    }

    @FXML
    protected void onRegisterLinkClick() {
        try {
            Stage stage;
            if (usernameField != null && usernameField.getScene() != null) {
                stage = (Stage) usernameField.getScene().getWindow();
            } else if (statusLabel != null && statusLabel.getScene() != null) {
                stage = (Stage) statusLabel.getScene().getWindow();
            } else {
                // As a fallback, open a new stage
                stage = new Stage();
            }

            FXMLLoader fxmlLoader = new FXMLLoader(SigninActivityForUser.class.getResource("SigninActivityForUser.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 400);
            stage.setTitle("Register - Restaurant Management");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleAdminLogin(String email, String password) {
        // Hardcoded admin login (admin@gmail.com / admin123 per requirements)
        if ("admin@gmail.com".equals(email) && "admin123".equals(password)) {
            if (statusLabel != null) {
                statusLabel.setText("Admin login successful!");
                statusLabel.setStyle("-fx-text-fill: green;");
            }
            // Navigate to Admin Dashboard
            try {
                Stage stage = (Stage) usernameField.getScene().getWindow();
                FXMLLoader fxmlLoader = new FXMLLoader(AdminDashboard.class.getResource("AdminDashboard.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 900, 600);
                stage.setTitle("Admin Dashboard - Restaurant Management");
                stage.setScene(scene);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            if (statusLabel != null) {
                statusLabel.setText("Invalid admin credentials.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }

    private void handleUserLogin(String email, String password) {
        if (DatabaseHelper.validateUser(email, password)) {
            if (statusLabel != null) {
                statusLabel.setText("User login successful!");
                statusLabel.setStyle("-fx-text-fill: green;");
            }
            // Navigate to User Dashboard
            try {
                Stage stage = (Stage) usernameField.getScene().getWindow();
                FXMLLoader fxmlLoader = new FXMLLoader(UserDashboard.class.getResource("UserDashboard.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 800, 600);
                stage.setTitle("User Dashboard - Restaurant Management");
                stage.setScene(scene);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            if (statusLabel != null) {
                statusLabel.setText("Invalid user credentials.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
