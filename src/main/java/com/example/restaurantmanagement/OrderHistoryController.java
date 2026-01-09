package com.example.restaurantmanagement;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.util.List;

public class OrderHistoryController {

    @FXML private ListView<String> orderHistoryListView;

    private UserDashboard dashboardController;

    public void initData(UserDashboard dashboardController) {
        this.dashboardController = dashboardController;
        refreshOrderHistory();
    }

    private void refreshOrderHistory() {
        if (orderHistoryListView == null) return;
        List<String> history = DatabaseHelper.getOrderHistory(dashboardController.getCurrentUserEmail());
        orderHistoryListView.setItems(FXCollections.observableArrayList(history));
    }

    @FXML
    protected void onBackClick() {
        dashboardController.onViewMenuClick();
    }
}
