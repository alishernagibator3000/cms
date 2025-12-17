package com.example.cms.controllers;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import com.example.cms.models.Student;
import com.example.cms.database.Database;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CourseController implements Initializable {

    private int userId;

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupColumns();
        studentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        studentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> fillForm(newSel));
    }

    public void setUser(int userId) {
        this.userId = userId;
        try { loadStudents(); }
        catch (SQLException e) { showError("DB Error", e.getMessage()); }
    }

    @FXML
    protected void loadStudents() throws SQLException {
        ObservableList<Student> list = Database.getAllStudentsForUser(userId);
        studentsTable.setItems(list);
        if (list.isEmpty()) clearForm();
    }

    @FXML
    protected void addStudent() {
        Student student = getFormData();
        if (student == null) return;
        try {
            Database.addStudentForUser(student, userId);
            showInfo("Success", "Student added");
            reload();
        } catch (SQLException e) { showError("DB Error", e.getMessage()); }
    }

    @FXML
    protected void deleteStudent() {
        Student selected = studentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showWarning("Select student", "Choose student to delete"); return; }

        if (confirmAction("Delete student", "Delete student: " + selected.getName() + " " + selected.getSurname() + "?")) {
            try {
                Database.deleteStudentForUser(selected.getId(), userId);
                showInfo("Deleted", "Student removed");
                reload();
            } catch (SQLException e) { showError("DB Error", e.getMessage()); }
        }
    }

    @FXML
    protected void editStudent() {
        Student selected = studentsTable.getSelectionModel().getSelectedItem();
        Student student = getFormData();
        if (selected == null) { showWarning("Select student", "Choose a student to edit."); return; }
        if (student == null) return;
        if (student.getId() != selected.getId()) {
            showWarning("ID change", "Changing student ID is not allowed.");
            return;
        }

        try {
            Database.updateStudentForUser(student, userId);
            showInfo("Updated", "Student updated.");
            reload();
        } catch (SQLException e) { showError("DB Error", e.getMessage()); }
    }

    @FXML
    protected void searchStudent() {
        String text = search.getText().trim();
        try {
            if (text.length() < 2) { loadStudents(); return; }
            studentsTable.setItems(Database.searchStudentsForUser(text, userId));
        } catch (SQLException e) { showError("DB Error", e.getMessage()); }
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
        if (s == null) { clearForm(); return; }
        studentId.setText(String.valueOf(s.getId()));
        studentId.setEditable(false);
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
            showWarning("Validation", "Fields ID, Name and Surname are required");
            return null;
        }

        try {
            int id = Integer.parseInt(idStr);
            return new Student(id, n, sn, f, d, g);
        } catch (NumberFormatException e) {
            showWarning("Validation", "Student ID must be a number");
            return null;
        }
    }

    private void clearForm() {
        studentId.clear();
        studentId.setEditable(true);
        name.clear();
        surname.clear();
        faculty.clear();
        department.clear();
        group.clear();
        studentsTable.getSelectionModel().clearSelection();
    }

    private boolean confirmAction(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        a.setTitle(title);
        return a.showAndWait().filter(btn -> btn == ButtonType.YES).isPresent();
    }

    private void showError(String title, String msg) { showAlert(Alert.AlertType.ERROR, title, msg); }
    private void showWarning(String title, String msg) { showAlert(Alert.AlertType.WARNING, title, msg); }
    private void showInfo(String title, String msg) { showAlert(Alert.AlertType.INFORMATION, title, msg); }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type, msg, ButtonType.OK);
        a.setTitle(title);
        a.showAndWait();
    }

    private void reload() throws SQLException {
        clearForm();
        loadStudents();
    }
}