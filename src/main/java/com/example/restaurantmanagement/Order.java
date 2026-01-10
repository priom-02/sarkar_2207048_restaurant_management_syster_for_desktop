package com.example.restaurantmanagement;

public class Order {
    private int id;
    private String transactionId;
    private final String userEmail;
    private String itemName; // Can be a list of items concatenated
    private int quantity;    // Can be total quantity
    private double totalPrice;
    private final String orderDate;
    private String status;

    // User details
    private final String userName;
    private final String userMobile;
    private final String userAddress;

    public Order(int id, String userEmail, String itemName, int quantity, double totalPrice, String orderDate, String status, String userName, String userMobile, String userAddress) {
        this.id = id;
        this.userEmail = userEmail;
        this.itemName = itemName;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.orderDate = orderDate;
        this.status = status;
        this.userName = userName;
        this.userMobile = userMobile;
        this.userAddress = userAddress;
    }

    // Getters
    public int getId() { return id; }
    public String getTransactionId() { return transactionId; }
    public String getUserEmail() { return userEmail; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public double getTotalPrice() { return totalPrice; }
    public String getOrderDate() { return orderDate; }
    public String getStatus() { return status; }
    public String getUserName() { return userName; }
    public String getUserMobile() { return userMobile; }
    public String getUserAddress() { return userAddress; }

    // Setters for grouped data
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public void setStatus(String status) { this.status = status; }
}
