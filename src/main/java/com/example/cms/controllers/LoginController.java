package com.example.cms.controllers;

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
import com.example.cms.models.Session;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ProgressIndicator loadingIndicator;

    @FXML
    public void initialize() {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }
    }

    @FXML
    protected void onLogin() {
        String u = usernameField.getText().trim();
        String p = passwordField.getText().trim();

        if (u.isEmpty() || p.isEmpty()) {
            showError("Validation Error", "Username and password are required");
            return;
        }

        showLoading(true);

        // Run in background thread to avoid UI freeze
        new Thread(() -> {
            try {
                if (Database.validateUser(u, p)) {
                    int id = Database.getUserId(u);
                    Session.currentUserId = id;

                    javafx.application.Platform.runLater(() -> {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cms/course.fxml"));
                            Scene scene = new Scene(loader.load());

                            CourseController controller = loader.getController();
                            controller.setUser(id);

                            HelloApplication.getPrimaryStage().setTitle("Course Management System");
                            HelloApplication.getPrimaryStage().setScene(scene);
                        } catch (IOException e) {
                            showError("Navigation Error", "Failed to load main screen: " + e.getMessage());
                        } finally {
                            showLoading(false);
                        }
                    });
                } else {
                    javafx.application.Platform.runLater(() -> {
                        showLoading(false);
                        showError("Login Failed", "Invalid username or password");
                    });
                }
            } catch (SQLException e) {
                javafx.application.Platform.runLater(() -> {
                    showLoading(false);
                    showError("Database Error", "Failed to connect to database: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    protected void goToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cms/register.fxml"));
            Scene scene = new Scene(loader.load());
            HelloApplication.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            showError("Navigation Error", "Failed to load registration screen: " + e.getMessage());
        }
    }

    private void showLoading(boolean show) {
        if (loadingIndicator != null) {
            javafx.application.Platform.runLater(() -> loadingIndicator.setVisible(show));
        }
    }

    private void showError(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}