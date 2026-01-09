package com.example.restaurantmanagement;

public class MenuItem {
    private int id;
    private String name; // Corresponds to item_name in DB
    private String category;
    private double price;
    private String status; // Available, Out of Stock, etc.
    private String description;
    private String imagePath;
    
    // Legacy field for backward compatibility if needed, mapped to status
    private boolean available;

    public MenuItem() {}

    public MenuItem(int id, String name, String category, double price, String status, String description, String imagePath) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.status = status;
        this.description = description;
        this.imagePath = imagePath;
        this.available = "Available".equalsIgnoreCase(status);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { 
        this.status = status; 
        this.available = "Available".equalsIgnoreCase(status);
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { 
        this.available = available;
        if (this.status == null || this.status.isEmpty()) {
            this.status = available ? "Available" : "Out of Stock";
        }
    }
}
