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
        System.out.println("Database file location: " + java.nio.file.Paths.get("restaurant_management.db").toAbsolutePath());
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            // 1. users Table
            String sqlUsers = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "email TEXT NOT NULL UNIQUE," +
                    "password TEXT NOT NULL," +
                    "role TEXT DEFAULT 'User'," +
                    "mobile TEXT," +
                    "address TEXT" +
                    ");";
            stmt.execute(sqlUsers);

            // 2. menu_items Table
            String sqlMenu = "CREATE TABLE IF NOT EXISTS menu_items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "item_name TEXT NOT NULL," +
                    "category TEXT," +
                    "price REAL NOT NULL," +
                    "status TEXT DEFAULT 'Available'," +
                    "description TEXT," +
                    "image_path TEXT" +
                    ");";
            stmt.execute(sqlMenu);

            // 3. orders Table
            String sqlOrders = "CREATE TABLE IF NOT EXISTS orders (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_email TEXT NOT NULL," +
                    "item_name TEXT NOT NULL," +
                    "quantity INTEGER NOT NULL," +
                    "total_price REAL NOT NULL," +
                    "order_date TEXT DEFAULT CURRENT_TIMESTAMP" +
                    ");";
            stmt.execute(sqlOrders);

            // Migrations
            try { stmt.execute("ALTER TABLE users ADD COLUMN role TEXT DEFAULT 'User';"); } catch (SQLException ignore) {}
            try { stmt.execute("ALTER TABLE users ADD COLUMN mobile TEXT;"); } catch (SQLException ignore) {}
            try { stmt.execute("ALTER TABLE users ADD COLUMN address TEXT;"); } catch (SQLException ignore) {}
            try { stmt.execute("ALTER TABLE menu_items ADD COLUMN category TEXT;"); } catch (SQLException ignore) {}
            try { stmt.execute("ALTER TABLE menu_items ADD COLUMN status TEXT DEFAULT 'Available';"); } catch (SQLException ignore) {}
            try { stmt.execute("ALTER TABLE menu_items RENAME COLUMN name TO item_name;"); } catch (SQLException ignore) {}
            try { stmt.execute("ALTER TABLE menu_items DROP COLUMN available;"); } catch (SQLException ignore) {}

            System.out.println("Database initialized.");
            return true;
        } catch (SQLException e) {
            lastError = "Init Error: " + e.getMessage();
            System.out.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ... (hashPassword, registerUser, updateUserProfile, validateUser, getAllUsers, getUserByEmail, addMenuItem, updateMenuItem, deleteMenuItem, getAllMenuItems, storeImageFile, placeOrder methods remain same)

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

    public static boolean registerUser(String name, String email, String password, String mobile, String address) {
        String sql = "INSERT INTO users(name, email, password, role, mobile, address) VALUES(?, ?, ?, ?, ?, ?)";
        String hashed = hashPassword(password);
        if (hashed == null) return false;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, hashed);
            pstmt.setString(4, "User");
            pstmt.setString(5, mobile);
            pstmt.setString(6, address);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            lastError = "Register Error: " + e.getMessage();
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean registerUser(String name, String email, String password) {
        return registerUser(name, email, password, null, null);
    }

    public static boolean updateUserProfile(int id, String name, String mobile, String address) {
        String sql = "UPDATE users SET name = ?, mobile = ?, address = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, mobile);
            pstmt.setString(3, address);
            pstmt.setInt(4, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            lastError = "Update Profile Error: " + e.getMessage();
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
            if (!rs.next()) return false;
            String stored = rs.getString("password");
            String hashedProvided = hashPassword(password);
            if (hashedProvided == null) return false;
            if (stored.equals(hashedProvided)) return true;
            if (stored.equals(password)) {
                String updateSql = "UPDATE users SET password = ? WHERE email = ?";
                try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                    updatePstmt.setString(1, hashedProvided);
                    updatePstmt.setString(2, email);
                    updatePstmt.executeUpdate();
                } catch (SQLException ignore) {}
                return true;
            }
            return false;
        } catch (SQLException e) {
            lastError = "Login Error: " + e.getMessage();
            e.printStackTrace();
            return false;
        }
    }

    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, name, email, mobile, address FROM users";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new User(rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getString("mobile"), rs.getString("address")));
            }
        } catch (SQLException e) {
            lastError = "List Users Error: " + e.getMessage();
            e.printStackTrace();
        }
        return users;
    }

    public static User getUserByEmail(String email) {
        String sql = "SELECT id, name, email, mobile, address FROM users WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getString("mobile"), rs.getString("address"));
            }
        } catch (SQLException e) {
            lastError = "Get User Error: " + e.getMessage();
            e.printStackTrace();
        }
        return null;
    }

    public static boolean addMenuItem(String itemName, String category, double price, String status, String description, String imagePath) {
        String sql = "INSERT INTO menu_items(item_name, category, price, status, description, image_path) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, itemName);
            pstmt.setString(2, category);
            pstmt.setDouble(3, price);
            pstmt.setString(4, status);
            pstmt.setString(5, description);
            pstmt.setString(6, imagePath);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            lastError = "Add Menu Item Error: " + e.getMessage();
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateMenuItem(int id, String itemName, String category, double price, String status, String description, String imagePath) {
        String sql = "UPDATE menu_items SET item_name = ?, category = ?, price = ?, status = ?, description = ?, image_path = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, itemName);
            pstmt.setString(2, category);
            pstmt.setDouble(3, price);
            pstmt.setString(4, status);
            pstmt.setString(5, description);
            pstmt.setString(6, imagePath);
            pstmt.setInt(7, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            lastError = "Update Menu Item Error: " + e.getMessage();
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
            return true;
        } catch (SQLException e) {
            lastError = "Delete Error: " + e.getMessage();
            return false;
        }
    }

    public static List<MenuItem> getAllMenuItems() {
        List<MenuItem> menuItems = new ArrayList<>();
        String sql = "SELECT id, item_name, category, price, status, description, image_path FROM menu_items";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                MenuItem item = new MenuItem();
                item.setId(rs.getInt("id"));
                item.setName(rs.getString("item_name"));
                item.setCategory(rs.getString("category"));
                item.setPrice(rs.getDouble("price"));
                item.setStatus(rs.getString("status"));
                item.setDescription(rs.getString("description"));
                item.setImagePath(rs.getString("image_path"));
                item.setAvailable("Available".equalsIgnoreCase(rs.getString("status")));
                menuItems.add(item);
            }
        } catch (SQLException e) {
            lastError = "List Menu Items Error: " + e.getMessage();
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

    public static boolean placeOrder(String userEmail, String itemName, int quantity, double totalPrice) {
        String sql = "INSERT INTO orders(user_email, item_name, quantity, total_price) VALUES(?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userEmail);
            pstmt.setString(2, itemName);
            pstmt.setInt(3, quantity);
            pstmt.setDouble(4, totalPrice);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            lastError = "Place Order Error: " + e.getMessage();
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> getOrderHistory(String userEmail) {
        List<String> history = new ArrayList<>();
        String sql = "SELECT item_name, quantity, total_price, order_date FROM orders WHERE user_email = ? ORDER BY order_date DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userEmail);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String item = rs.getString("item_name");
                int qty = rs.getInt("quantity");
                double total = rs.getDouble("total_price");
                String date = rs.getString("order_date");
                history.add(String.format("%s | %s x%d | $%.2f", date, item, qty, total));
            }
        } catch (SQLException e) {
            lastError = "Get History Error: " + e.getMessage();
            e.printStackTrace();
        }
        return history;
    }
    
    public static List<String> getAllOrders() {
        List<String> history = new ArrayList<>();
        String sql = "SELECT user_email, item_name, quantity, total_price, order_date FROM orders ORDER BY order_date DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String email = rs.getString("user_email");
                String item = rs.getString("item_name");
                int qty = rs.getInt("quantity");
                double total = rs.getDouble("total_price");
                String date = rs.getString("order_date");
                history.add(String.format("%s | %s | %s x%d | $%.2f", date, email, item, qty, total));
            }
        } catch (SQLException e) {
            lastError = "Get All Orders Error: " + e.getMessage();
            e.printStackTrace();
        }
        return history;
    }
}
