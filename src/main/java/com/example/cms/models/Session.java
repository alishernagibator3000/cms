package com.example.cms.models;

public class Session {
    private static int currentUserId = -1;

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static void setCurrentUserId(int userId) {
        currentUserId = userId;
    }

    public static boolean isLoggedIn() {
        return currentUserId != -1;
    }

    public static void logout() {
        currentUserId = -1;
    }
}