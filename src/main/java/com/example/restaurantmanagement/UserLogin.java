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
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(UserLogin.class.getResource("UserLogin.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 400);
        stage.setTitle("Login - Restaurant Management");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    protected void onLoginButtonClick() {
        String role = roleComboBox.getValue();
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (role == null) {
            statusLabel.setText("Please select a role.");
            return;
        }

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password.");
            return;
        }

        if ("Admin".equals(role)) {
            handleAdminLogin(username, password);
        } else if ("User".equals(role)) {
            handleUserLogin(username, password);
        }
    }

    private void handleAdminLogin(String username, String password) {
        // Hardcoded admin login
        if ("admin".equals(username) && "admin123".equals(password)) {
            statusLabel.setText("Admin login successful!");
            statusLabel.setStyle("-fx-text-fill: green;");
            // TODO: Navigate to Admin Dashboard
        } else {
            statusLabel.setText("Invalid admin credentials.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void handleUserLogin(String username, String password) {
        // Placeholder for Firebase-based user login
        // Since Firebase dependencies are not yet set up, we'll simulate a check or print a message
        statusLabel.setText("User login logic (Firebase) not implemented yet.");
        statusLabel.setStyle("-fx-text-fill: orange;");
        System.out.println("Attempting user login for: " + username);
        
        // TODO: Implement Firebase authentication here
    }

    public static void main(String[] args) {
        launch();
    }
}
