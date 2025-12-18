package com.example.cms.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ProgressIndicator;
import com.example.cms.database.Database;
import com.example.cms.HelloApplication;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ProgressIndicator loadingIndicator;

    private ExecutorService executorService;

    @FXML
    public void initialize() {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }
        executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        });
    }

    @FXML
    protected void onRegister() {
        String u = usernameField.getText().trim();
        String p = passwordField.getText().trim();
        String c = confirmPasswordField.getText().trim();

        if (u.isEmpty() || p.isEmpty()) {
            showError("Validation Error", "Username and password are required");
            return;
        }

        if (u.length() < 3) {
            showError("Validation Error", "Username must be at least 3 characters long");
            return;
        }

        if (p.length() < 4) {
            showError("Validation Error", "Password must be at least 4 characters long");
            return;
        }

        if (!p.equals(c)) {
            showError("Validation Error", "Passwords do not match");
            return;
        }

        showLoading(true);

        executorService.submit(() -> {
            try {
                boolean ok = Database.registerUser(u, p);
                Platform.runLater(() -> {
                    showLoading(false);
                    if (ok) {
                        Alert a = new Alert(Alert.AlertType.INFORMATION);
                        a.setTitle("Success");
                        a.setHeaderText(null);
                        a.setContentText("Account created successfully! Please login with your credentials.");
                        a.showAndWait();
                        goToLogin();
                    } else {
                        showError("Registration Failed", "Username already exists. Please choose a different username.");
                    }
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    showLoading(false);
                    showError("Database Error", "Failed to register user: " + e.getMessage());
                });
            }
        });
    }

    @FXML
    protected void goToLogin() {
        try {
            cleanup();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cms/login.fxml"));
            Scene scene = new Scene(loader.load());
            HelloApplication.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            showError("Navigation Error", "Failed to load login screen: " + e.getMessage());
        }
    }

    private void showLoading(boolean show) {
        if (loadingIndicator != null) {
            Platform.runLater(() -> loadingIndicator.setVisible(show));
        }
    }

    private void showError(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}