package com.example.restaurantmanagement;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    // Use absolute path or a path relative to the user directory to ensure we are looking at the right file.
    // "restaurant_management.db" creates the file in the working directory of the process.
    private static final String DB_URL = "jdbc:sqlite:restaurant_management.db";
    private static String lastError = "";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found: " + e.getMessage());
            lastError = "Driver not found: " + e.getMessage();
        }
    }

    public static String getLastError() {
        return lastError;
    }

    public static boolean initializeDatabase() {
        // Print the absolute path of the DB file being used for debugging
        System.out.println("Database file location: " + java.nio.file.Paths.get("restaurant_management.db").toAbsolutePath());
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String sqlUsers = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "email TEXT NOT NULL UNIQUE," +
                    "password TEXT NOT NULL" +
                    ");";
            stmt.execute(sqlUsers);

            String sqlMenu = "CREATE TABLE IF NOT EXISTS menu_items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "description TEXT," +
                    "price REAL NOT NULL," +
                    "available INTEGER DEFAULT 1," +
                    "image_path TEXT" +
                    ");";
            stmt.execute(sqlMenu);

            try {
                stmt.execute("ALTER TABLE menu_items ADD COLUMN available INTEGER DEFAULT 1;");
            } catch (SQLException ignore) { }
            try {
                stmt.execute("ALTER TABLE menu_items ADD COLUMN image_path TEXT;");
            } catch (SQLException ignore) { }

            System.out.println("Database initialized.");
            return true;
        } catch (SQLException e) {
            lastError = "Init Error: " + e.getMessage();
            System.out.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Hash the password using SHA-256. Returns hex string or null on error.
    private static String hashPassword(String password) {
        if (password == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            lastError = "Hash Error: " + e.getMessage();
            e.printStackTrace();
            return null;
        }
    }

    public static boolean registerUser(String name, String email, String password) {
        String sql = "INSERT INTO users(name, email, password) VALUES(?, ?, ?)";

        String hashed = hashPassword(password);
        if (hashed == null) {
            lastError = "Unable to hash password.";
            return false;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, hashed);
            pstmt.executeUpdate();
            System.out.println("User registered: " + email);
            return true;
        } catch (SQLException e) {
            lastError = "Register Error: " + e.getMessage();
            System.out.println("Error registering user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean validateUser(String email, String password) {
        String selectSql = "SELECT password FROM users WHERE email = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                return false;
            }

            String stored = rs.getString("password");
            String hashedProvided = hashPassword(password);
            if (hashedProvided == null) {
                lastError = "Unable to hash password.";
                return false;
            }

            if (stored.equals(hashedProvided)) {
                return true;
            }

            if (stored.equals(password)) {
                String updateSql = "UPDATE users SET password = ? WHERE email = ?";
                try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                    updatePstmt.setString(1, hashedProvided);
                    updatePstmt.setString(2, email);
                    updatePstmt.executeUpdate();
                    System.out.println("Upgraded plaintext password to hashed for: " + email);
                } catch (SQLException ex) {
                    System.err.println("Failed to upgrade password hash: " + ex.getMessage());
                }
                return true;
            }

            return false;
        } catch (SQLException e) {
            lastError = "Login Error: " + e.getMessage();
            System.out.println("Error validating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, name, email FROM users";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new User(rs.getInt("id"), rs.getString("name"), rs.getString("email")));
            }
        } catch (SQLException e) {
            lastError = "List Users Error: " + e.getMessage();
            System.out.println("Error listing users: " + e.getMessage());
            e.printStackTrace();
        }

        return users;
    }

    public static boolean addMenuItem(String name, String description, double price, boolean available, String imagePath) {
        String sql = "INSERT INTO menu_items(name, description, price, available, image_path) VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setDouble(3, price);
            pstmt.setInt(4, available ? 1 : 0);
            pstmt.setString(5, imagePath);
            pstmt.executeUpdate();
            System.out.println("Menu item added: " + name);
            return true;
        } catch (SQLException e) {
            lastError = "Add Menu Item Error: " + e.getMessage();
            System.out.println("Error adding menu item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateMenuItem(int id, String name, String description, double price, boolean available, String imagePath) {
        String sql = "UPDATE menu_items SET name = ?, description = ?, price = ?, available = ?, image_path = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setDouble(3, price);
            pstmt.setInt(4, available ? 1 : 0);
            pstmt.setString(5, imagePath);
            pstmt.setInt(6, id);
            pstmt.executeUpdate();
            System.out.println("Menu item updated: " + name);
            return true;
        } catch (SQLException e) {
            lastError = "Update Menu Item Error: " + e.getMessage();
            System.out.println("Error updating menu item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteMenuItem(int id) {
        String sql = "DELETE FROM menu_items WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Menu item deleted with ID: " + id);
            return true;
        } catch (SQLException e) {
            lastError = "Delete Menu Item Error: " + e.getMessage();
            System.out.println("Error deleting menu item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static List<MenuItem> getAllMenuItems() {
        List<MenuItem> menuItems = new ArrayList<>();
        String sql = "SELECT id, name, description, price, available, image_path FROM menu_items";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                MenuItem item = new MenuItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getInt("available") == 1,
                        rs.getString("image_path")
                );
                menuItems.add(item);
            }
        } catch (SQLException e) {
            lastError = "List Menu Items Error: " + e.getMessage();
            System.out.println("Error listing menu items: " + e.getMessage());
            e.printStackTrace();
        }

        return menuItems;
    }

    public static String storeImageFile(Path source) {
        try {
            Path imagesDir = Path.of(System.getProperty("user.dir"), "images");
            if (!Files.exists(imagesDir)) Files.createDirectories(imagesDir);
            Path dest = imagesDir.resolve(source.getFileName());
            int count = 1;
            String base = dest.getFileName().toString();
            String name = base;
            while (Files.exists(dest)) {
                String suffix = "(" + count + ")";
                int dot = base.lastIndexOf('.');
                if (dot > 0) {
                    name = base.substring(0, dot) + suffix + base.substring(dot);
                } else {
                    name = base + suffix;
                }
                dest = imagesDir.resolve(name);
                count++;
            }
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
            return dest.toString();
        } catch (Exception e) {
            lastError = "Image Store Error: " + e.getMessage();
            e.printStackTrace();
            return null;
        }
    }
}
