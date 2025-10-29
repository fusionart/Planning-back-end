package com.monbat.planning.utils;

import com.monbat.planning.models.entities.User;

public class UserSession {
    private static UserSession instance;
    private String username;
    private String password;

    // Private constructor to prevent instantiation
    private UserSession() {
    }

    // Get the singleton instance
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // Set user credentials
    public void setUser(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Set user credentials from User entity
    public void setUser(User user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
    }

    // Get username
    public String getUsername() {
        return username;
    }

    // Get password
    public String getPassword() {
        return password;
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        return username != null && !username.isEmpty();
    }

    // Clear session (logout)
    public void clearSession() {
        this.username = null;
        this.password = null;
    }
}
