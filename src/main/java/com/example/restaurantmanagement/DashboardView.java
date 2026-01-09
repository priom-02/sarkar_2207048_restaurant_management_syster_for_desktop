package com.example.restaurantmanagement;

import java.util.List;

public interface DashboardView {
    void setUser(User user);
    void showMenu(List<MenuItem> menu);
    void onMenuItemSelected(MenuItem item);
    void onLogout();
}

