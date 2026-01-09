package com.example.restaurantmanagement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // OPTION 1: MySQL (Use this if you have XAMPP/WAMP running)
    // Ensure you create a database named 'restaurant_db' in phpMyAdmin first.
    private static final String DB_URL = "jdbc:mysql://localhost:3306/restaurant_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = ""; // Default XAMPP password is empty

    // OPTION 2: SQLite (Uncomment these lines if you prefer SQLite)
    // private static final String DB_URL = "jdbc:sqlite:restaurant_app.db";
    // private static final String DB_USER = "";
    // private static final String DB_PASSWORD = "";

    public Connection getConnection() {
        Connection databaseLink = null;

        try {
            // Attempt to establish a connection
            databaseLink = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            System.out.println("Database Connection Failed!");
            e.printStackTrace();
        }

        return databaseLink;
    }
}