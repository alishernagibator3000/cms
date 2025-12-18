package com.example.cms.controllers;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import com.example.cms.models.Student;
import com.example.cms.database.Database;
import com.example.cms.HelloApplication;
import com.example.cms.models.Session;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class CourseController implements Initializable {

    private int userId;
    private Timer searchTimer;
    private boolean isEditMode = false;
    private Student editingStudent = null;

    @FXML private TableView<Student> studentsTable;
    @FXML private TableColumn<Student, Integer> colId;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, String> colSurname;
    @FXML private TableColumn<Student, String> colFaculty;
    @FXML private TableColumn<Student, String> colDepartment;
    @FXML private TableColumn<Student, String> colGroup;

    @FXML private TextField studentId;
    @FXML private TextField name;
    @FXML private TextField surname;
    @FXML private TextField faculty;
    @FXML private TextField department;
    @FXML private TextField group;
    @FXML private TextField search;

    @FXML private Label formTitle;
    @FXML private Button actionButton;
    @FXML private Button cancelButton;
    @FXML private ProgressIndicator loadingIndicator;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupColumns();
        studentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        studentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null && !isEditMode) {
                fillForm(newSel);
            }
        });

        // Enable sorting
        colId.setSortable(true);
        colName.setSortable(true);
        colSurname.setSortable(true);
        studentsTable.getSortOrder().add(colId);

        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }
    }

    public void setUser(int userId) {
        this.userId = userId;
        try {
            showLoading(true);
            loadStudents();
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
        } finally {
            showLoading(false);
        }
    }

    @FXML
    protected void loadStudents() throws SQLException {
        showLoading(true);
        try {
            ObservableList<Student> list = Database.getAllStudentsForUser(userId);
            studentsTable.setItems(list);
            studentsTable.sort();
            if (list.isEmpty()) {
                clearForm();
            }
        } finally {
            showLoading(false);
        }
    }

    @FXML
    protected void addStudent() {
        if (isEditMode) {
            showWarning("Edit Mode", "You are in edit mode. Click Cancel first or finish editing.");
            return;
        }

        Student student = getFormData();
        if (student == null) return;

        showLoading(true);
        try {
            Database.addStudentForUser(student, userId);
            showInfo("Success", "Student added successfully");
            reload();
        } catch (SQLException e) {
            if (e.getMessage().contains("already exists")) {
                showError("Duplicate ID", e.getMessage());
            } else {
                showError("Database Error", e.getMessage());
            }
        } finally {
            showLoading(false);
        }
    }

    @FXML
    protected void deleteStudent() {
        Student selected = studentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select a student to delete");
            return;
        }

        if (confirmAction("Delete Student", "Are you sure you want to delete student: " + selected.getName() + " " + selected.getSurname() + "?")) {
            showLoading(true);
            try {
                int rowsAffected = Database.deleteStudentForUser(selected.getId(), userId);
                if (rowsAffected > 0) {
                    showInfo("Deleted", "Student removed successfully");
                    reload();
                } else {
                    showWarning("Not Found", "Student not found or already deleted");
                }
            } catch (SQLException e) {
                showError("Database Error", e.getMessage());
            } finally {
                showLoading(false);
            }
        }
    }

    @FXML
    protected void editStudent() {
        Student selected = studentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select a student to edit");
            return;
        }

        if (!isEditMode) {
            enterEditMode(selected);
        } else {
            saveEdit();
        }
    }

    private void enterEditMode(Student student) {
        isEditMode = true;
        editingStudent = student;
        fillForm(student);
        studentId.setEditable(false);
        studentId.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #d0d0d0; -fx-border-width: 1; -fx-border-radius: 3; -fx-background-radius: 3; -fx-padding: 8 12; -fx-font-size: 13px;");

        if (formTitle != null) {
            formTitle.setText("Edit Student");
            formTitle.setStyle("-fx-text-fill: #ff6b6b;");
        }
        if (actionButton != null) {
            actionButton.setText("Save Changes");
            actionButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: white; -fx-background-radius: 3; -fx-font-size: 14px; -fx-cursor: hand; -fx-border-width: 0;");
        }
        if (cancelButton != null) {
            cancelButton.setVisible(true);
        }
    }

    private void saveEdit() {
        Student student = getFormData();
        if (student == null) return;

        if (student.getId() != editingStudent.getId()) {
            showWarning("Invalid Operation", "Student ID cannot be changed");
            return;
        }

        showLoading(true);
        try {
            int rowsAffected = Database.updateStudentForUser(student, userId);
            if (rowsAffected > 0) {
                showInfo("Updated", "Student updated successfully");
                exitEditMode();
                reload();
            } else {
                showWarning("Not Found", "Student not found. It may have been deleted.");
            }
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
        } finally {
            showLoading(false);
        }
    }

    @FXML
    protected void cancelEdit() {
        exitEditMode();
        clearForm();
    }

    private void exitEditMode() {
        isEditMode = false;
        editingStudent = null;
        studentId.setEditable(true);
        studentId.setStyle("-fx-background-color: white; -fx-border-color: #d0d0d0; -fx-border-width: 1; -fx-border-radius: 3; -fx-background-radius: 3; -fx-padding: 8 12; -fx-font-size: 13px;");

        if (formTitle != null) {
            formTitle.setText("Add New Student");
            formTitle.setStyle("-fx-text-fill: #333333;");
        }
        if (actionButton != null) {
            actionButton.setText("Add Student");
            actionButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 3; -fx-font-size: 14px; -fx-cursor: hand; -fx-border-width: 0;");
        }
        if (cancelButton != null) {
            cancelButton.setVisible(false);
        }
    }

    @FXML
    protected void searchStudent() {
        if (searchTimer != null) {
            searchTimer.cancel();
        }

        searchTimer = new Timer();
        searchTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    String text = search.getText().trim();
                    showLoading(true);
                    try {
                        if (text.length() < 1) {
                            loadStudents();
                            return;
                        }
                        ObservableList<Student> results = Database.searchStudentsForUser(text, userId);
                        studentsTable.setItems(results);
                        studentsTable.sort();
                    } catch (SQLException e) {
                        showError("Database Error", e.getMessage());
                    } finally {
                        showLoading(false);
                    }
                });
            }
        }, 300);
    }

    @FXML
    protected void logout() {
        if (confirmAction("Logout", "Are you sure you want to logout?")) {
            Session.currentUserId = -1;

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cms/login.fxml"));
                Scene scene = new Scene(loader.load());
                HelloApplication.getPrimaryStage().setTitle("Course Management System - Login");
                HelloApplication.getPrimaryStage().setScene(scene);
            } catch (IOException e) {
                showError("Navigation Error", "Failed to return to login screen: " + e.getMessage());
            }
        }
    }

    private void setupColumns() {
        colId.setCellValueFactory(param -> new javafx.beans.property.SimpleIntegerProperty(param.getValue().getId()).asObject());
        colName.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getName()));
        colSurname.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getSurname()));
        colFaculty.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getFaculty()));
        colDepartment.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getDepartment()));
        colGroup.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getGroup()));
    }

    private void fillForm(Student s) {
        if (s == null) {
            clearForm();
            return;
        }
        studentId.setText(String.valueOf(s.getId()));
        name.setText(s.getName());
        surname.setText(s.getSurname());
        faculty.setText(s.getFaculty());
        department.setText(s.getDepartment());
        group.setText(s.getGroup());
    }

    private Student getFormData() {
        String idStr = studentId.getText().trim();
        String n = name.getText().trim();
        String sn = surname.getText().trim();
        String f = faculty.getText().trim();
        String d = department.getText().trim();
        String g = group.getText().trim();

        if (idStr.isEmpty() || n.isEmpty() || sn.isEmpty()) {
            showWarning("Validation Error", "Student ID, Name and Surname are required fields");
            return null;
        }

        try {
            int id = Integer.parseInt(idStr);
            if (id <= 0) {
                showWarning("Validation Error", "Student ID must be a positive number");
                return null;
            }
            return new Student(id, n, sn, f, d, g);
        } catch (NumberFormatException e) {
            showWarning("Validation Error", "Student ID must be a valid number");
            return null;
        }
    }

    private void clearForm() {
        studentId.clear();
        name.clear();
        surname.clear();
        faculty.clear();
        department.clear();
        group.clear();
        studentsTable.getSelectionModel().clearSelection();
        if (isEditMode) {
            exitEditMode();
        }
    }

    private void showLoading(boolean show) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(show);
        }
        if (studentsTable != null) {
            studentsTable.setDisable(show);
        }
    }

    private boolean confirmAction(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        a.setTitle(title);
        a.setHeaderText(null);
        return a.showAndWait().filter(btn -> btn == ButtonType.YES).isPresent();
    }

    private void showError(String title, String msg) {
        showAlert(Alert.AlertType.ERROR, title, msg);
    }

    private void showWarning(String title, String msg) {
        showAlert(Alert.AlertType.WARNING, title, msg);
    }

    private void showInfo(String title, String msg) {
        showAlert(Alert.AlertType.INFORMATION, title, msg);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void reload() throws SQLException {
        clearForm();
        loadStudents();
    }
}