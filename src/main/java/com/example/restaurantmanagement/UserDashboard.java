package com.example.restaurantmanagement;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserDashboard {

    @FXML private StackPane contentArea;

    private String currentUserEmail;
    private List<CartItem> cart = new ArrayList<>();

    public void setCurrentUserEmail(String email) {
        this.currentUserEmail = email;
        // Load default view after user is set
        onViewMenuClick();
    }

    @FXML
    public void initialize() {
        // The default view will be loaded once the user email is set.
    }

    @FXML
    protected void onViewMenuClick() {
        loadView("ViewMenu.fxml");
    }

    @FXML
    protected void onViewCartClick() {
        loadView("Cart.fxml");
    }

    @FXML
    protected void onOrderHistoryClick() {
        loadView("OrderHistory.fxml");
    }

    @FXML
    protected void onProfileClick() {
        loadView("Profile.fxml");
    }

    @FXML
    protected void onLogoutClick() {
        try {
            Stage stage = (Stage) contentArea.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(UserLogin.class.getResource("UserLogin.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 400);
            stage.setTitle("Login - Restaurant Management");
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Node view = loader.load();
            
            // Pass data to the new controller
            Object controller = loader.getController();
            if (controller instanceof ViewMenuController) {
                ((ViewMenuController) controller).initData(this);
            } else if (controller instanceof CartController) {
                ((CartController) controller).initData(this);
            } else if (controller instanceof OrderHistoryController) {
                ((OrderHistoryController) controller).initData(this);
            } else if (controller instanceof ProfileController) {
                ((ProfileController) controller).initData(this);
            } else if (controller instanceof PaymentController) {
                ((PaymentController) controller).initData(this);
            } else if (controller instanceof ItemDetailController) {
                // This one is handled differently as it needs a specific item
            }

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void showItemDetail(MenuItem item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ItemDetail.fxml"));
            Node view = loader.load();
            ItemDetailController controller = loader.getController();
            controller.initData(this, item);
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- Getters for shared data ---
    public String getCurrentUserEmail() { return currentUserEmail; }
    public List<CartItem> getCart() { return cart; }
}
